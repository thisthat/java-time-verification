/*
 * Azureus - a Java Bittorrent client
 *
 * This program is free software; you can redistribute it and/or modify
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Created on Oct 18, 2003
 * Created by Paul Gardner
 * Modified Apr 13, 2004 by Alon Rohter
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 */

package org.gudy.azureus2.core3.disk.impl;

import com.aelitis.azureus.core.diskmanager.access.DiskAccessController;
import com.aelitis.azureus.core.diskmanager.access.DiskAccessControllerFactory;
import com.aelitis.azureus.core.diskmanager.cache.CacheFile;
import com.aelitis.azureus.core.diskmanager.cache.CacheFileManagerException;
import com.aelitis.azureus.core.diskmanager.cache.CacheFileManagerFactory;
import com.aelitis.azureus.core.diskmanager.cache.CacheFileOwner;
import com.aelitis.azureus.core.diskmanager.file.FMFileManagerFactory;
import com.aelitis.azureus.core.util.LinkFileMap;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.ParameterListener;
import org.gudy.azureus2.core3.disk.*;
import org.gudy.azureus2.core3.disk.impl.access.DMAccessFactory;
import org.gudy.azureus2.core3.disk.impl.access.DMChecker;
import org.gudy.azureus2.core3.disk.impl.access.DMReader;
import org.gudy.azureus2.core3.disk.impl.access.DMWriter;
import org.gudy.azureus2.core3.disk.impl.piecemapper.*;
import org.gudy.azureus2.core3.disk.impl.resume.RDResumeHandler;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerException;
import org.gudy.azureus2.core3.download.DownloadManagerState;
import org.gudy.azureus2.core3.download.DownloadManagerStats;
import org.gudy.azureus2.core3.download.impl.DownloadManagerMoveHandler;
import org.gudy.azureus2.core3.download.impl.DownloadManagerStatsImpl;
import org.gudy.azureus2.core3.internat.LocaleTorrentUtil;
import org.gudy.azureus2.core3.internat.LocaleUtilDecoder;
import org.gudy.azureus2.core3.internat.LocaleUtilEncodingException;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.PlatformManagerCapabilities;
import org.gudy.azureus2.platform.PlatformManagerFactory;
import org.gudy.azureus2.plugins.download.savelocation.SaveLocationChange;
import org.gudy.azureus2.plugins.platform.PlatformManagerException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 * The disk Wrapper.
 *
 * @author Tdv_VgA
 * @author MjrTom
 *          2005/Oct/08: new piece-picking support changes
 *          2006/Jan/02: refactoring piece picking related code
 *
 */

public class
DiskManagerImpl
    extends LogRelation
    implements DiskManagerHelper
{
	private static final int DM_FREE_PIECELIST_TIMEOUT	= 120*1000;
	
    private static final LogIDs LOGID = LogIDs.DISK;

    private static DiskAccessController disk_access_controller;

    static {
        int max_read_threads        = COConfigurationManager.getIntParameter( "diskmanager.perf.read.maxthreads" );
        int max_read_mb             = COConfigurationManager.getIntParameter( "diskmanager.perf.read.maxmb" );
        int max_write_threads       = COConfigurationManager.getIntParameter( "diskmanager.perf.write.maxthreads" );
        int max_write_mb            = COConfigurationManager.getIntParameter( "diskmanager.perf.write.maxmb" );

        disk_access_controller =
            DiskAccessControllerFactory.create(
            		"core",
                    max_read_threads, max_read_mb,
                    max_write_threads, max_write_mb );

        if (Logger.isEnabled()){
            Logger.log(
                    new LogEvent(
                            LOGID,
                            "Disk access controller params: " +
                                max_read_threads + "/" + max_read_mb + "/" + max_write_threads + "/" + max_write_mb ));

        }
    }

    public static DiskAccessController
    getDefaultDiskAccessController()
    {
        return( disk_access_controller );
    }
    
    private static boolean 	reorder_storage_mode;
    private static int		reorder_storage_mode_min_mb;
    
    static{
    	COConfigurationManager.addAndFireParameterListeners(
    		new String[]{
    			"Enable reorder storage mode",
    			"Reorder storage mode min MB" },
    		new ParameterListener()
    		{
    			public void 
    			parameterChanged(
    				String parameterName ) 
    			{
       				reorder_storage_mode 		= COConfigurationManager.getBooleanParameter( "Enable reorder storage mode" );
       				reorder_storage_mode_min_mb = COConfigurationManager.getIntParameter( "Reorder storage mode min MB" );
    			}
    		});
    }
    
    private static DiskManagerRecheckScheduler      recheck_scheduler       = new DiskManagerRecheckScheduler();
    private static DiskManagerAllocationScheduler   allocation_scheduler    = new DiskManagerAllocationScheduler();

    private static ThreadPool	start_pool = new ThreadPool( "DiskManager:start", 64, true );
    
    static{
    	start_pool.setThreadPriority( Thread.MIN_PRIORITY );
    }
    
    private boolean used    = false;

    private boolean started = false;
    private AESemaphore started_sem = new AESemaphore( "DiskManager::started" );
    private boolean starting;
    private boolean stopping;


    private int state_set_via_method;
    private String 	errorMessage = "";
    private int		errorType	= 0;
    
    private int pieceLength;
    private int lastPieceLength;

    private int         nbPieces;       // total # pieces in this torrent
    private long        totalLength;    // total # bytes in this torrent
    private int         percentDone;
    private long        allocated;
    private long        remaining;


    private TOTorrent       torrent;


    private DMReader                reader;
    private DMChecker               checker;
    private DMWriter                writer;

    private RDResumeHandler         resume_handler;
    private DMPieceMapper           piece_mapper;

    private DiskManagerPieceImpl[]  pieces;
    
	private DMPieceMap				piece_map_use_accessor;
	private long					piece_map_use_accessor_time;

    private DiskManagerFileInfoImpl[]				files;
	private DiskManagerFileInfoSet					fileset;
	
    protected DownloadManager       download_manager;

    private boolean alreadyMoved = false;

    private boolean             skipped_file_set_changed =true; // go over them once when starting
    private long                skipped_file_set_size;
    private long                skipped_but_downloaded;

    private AtomicLong			priority_change_marker = new AtomicLong( RandomUtils.nextLong());
    {
	    if ( priority_change_marker.get() == 0 ){
	    	priority_change_marker.incrementAndGet();
	    }
    }
    
    private boolean				checking_enabled = true;

    private volatile boolean	move_in_progress;
    private volatile int		move_progress;

        // DiskManager listeners

    private static final int LDT_STATECHANGED           = 1;
    private static final int LDT_PRIOCHANGED            = 2;
    private static final int LDT_PIECE_DONE_CHANGED     = 3;
    private static final int LDT_ACCESS_MODE_CHANGED    = 4;

    protected static ListenerManager<DiskManagerListener>    listeners_aggregator    = ListenerManager.createAsyncManager(
            "DiskM:ListenAggregatorDispatcher",
            new ListenerManagerDispatcher<DiskManagerListener>()
            {
                public void
                dispatch(
                	DiskManagerListener      listener,
                    int         			type,
                    Object      			value )
                {
                    if (type == LDT_STATECHANGED){

                        int params[] = (int[])value;

                        listener.stateChanged(params[0], params[1]);

                    }else if (type == LDT_PRIOCHANGED) {

                        listener.filePriorityChanged((DiskManagerFileInfo)value);

                    }else if (type == LDT_PIECE_DONE_CHANGED) {

                        listener.pieceDoneChanged((DiskManagerPiece)value);

                    }else if (type == LDT_ACCESS_MODE_CHANGED) {

                        Object[]    o = (Object[])value;

                        listener.fileAccessModeChanged(
                            (DiskManagerFileInfo)o[0],
                            ((Integer)o[1]).intValue(),
                            ((Integer)o[2]).intValue());
                    }
                }
            });

    private ListenerManager<DiskManagerListener> listeners   = ListenerManager.createManager(
            "DiskM:ListenDispatcher",
            new ListenerManagerDispatcher<DiskManagerListener>()
            {
                public void
                dispatch(
                	DiskManagerListener      listener,
                    int         			type,
                    Object      			value )
                {
                    listeners_aggregator.dispatch( listener, type, value );
                }
            });

    private AEMonitor   start_stop_mon  = new AEMonitor( "DiskManager:startStop" );
    private AEMonitor   file_piece_mon  = new AEMonitor( "DiskManager:filePiece" );

    
    public
    DiskManagerImpl(
        TOTorrent           _torrent,
        DownloadManager     _dmanager)
    {
        torrent             = _torrent;
        download_manager    = _dmanager;
       
        pieces      = new DiskManagerPieceImpl[0];  // in case things go wrong later

        setState( INITIALIZING );

        percentDone = 0;
        errorType	= ET_NONE;
        
        if ( torrent == null ){

            errorMessage     = "Torrent not available";

            setState( FAULTY );

            return;
        }

        LocaleUtilDecoder   locale_decoder = null;

        try{
            locale_decoder = LocaleTorrentUtil.getTorrentEncoding( torrent );

        }catch( TOTorrentException e ){

            Debug.printStackTrace( e );

            errorMessage = TorrentUtils.exceptionToText(e);

            setState( FAULTY );

            return;

        }catch( Throwable e ){

            Debug.printStackTrace( e );

            errorMessage = "Initialisation failed - " + Debug.getNestedExceptionMessage(e);

            setState( FAULTY );

            return;
        }

        piece_mapper    = DMPieceMapperFactory.create( torrent );

        try{
            piece_mapper.construct( locale_decoder, download_manager.getAbsoluteSaveLocation().getName());

        }catch( Throwable e ){

            Debug.printStackTrace( e );

            errorMessage = "Failed to build piece map - " + Debug.getNestedExceptionMessage(e);

            setState( FAULTY );

            return;
        }

        totalLength = piece_mapper.getTotalLength();
        remaining   = totalLength;

        nbPieces    = torrent.getNumberOfPieces();

        pieceLength     = (int)torrent.getPieceLength();
        lastPieceLength = piece_mapper.getLastPieceLength();

        pieces      = new DiskManagerPieceImpl[nbPieces];
        
        for (int i =0; i <nbPieces; i++)
        {
            pieces[i] =new DiskManagerPieceImpl(this, i, i==nbPieces-1?lastPieceLength:pieceLength);
        }

        reader          = DMAccessFactory.createReader(this);

        checker         = DMAccessFactory.createChecker(this);

        writer          = DMAccessFactory.createWriter(this);

        resume_handler  = new RDResumeHandler( this, checker );

    }

    public void
    start()
    {
        try{
        	if ( move_in_progress ){
        		
        		Debug.out( "start called while move in progress!" );
        	}
        	
            start_stop_mon.enter();

            if ( used ){

                Debug.out( "DiskManager reuse not supported!!!!" );
            }

            used    = true;

            if ( getState() == FAULTY ){

                Debug.out( "starting a faulty disk manager");

                return;
            }

            started     = true;
            starting    = true;

            start_pool.run(
            	new AERunnable()
            	{
                    public void
                    runSupport()
                    {
                        try{                       	
                        		// now we use a limited pool to manage disk manager starts there
                        		// is an increased possibility of us being stopped before starting
                        		// handle this situation better by avoiding an un-necessary "startSupport"
                        	
                            try{
                                start_stop_mon.enter();

	                        	if ( stopping ){
	                        		
	                        		throw( new Exception( "Stopped during startup" ));
	                        	}
                            }finally{

                                start_stop_mon.exit();
                            }
                            
                            startSupport();

                        }catch( Throwable e ){

                            errorMessage = Debug.getNestedExceptionMessage(e) + " (start)";
                            
                            Debug.printStackTrace(e);

                            setState( FAULTY );

                        }finally{

                            started_sem.release();
                        }

                        boolean stop_required;

                        try{
                            start_stop_mon.enter();

                            stop_required = DiskManagerImpl.this.getState() == DiskManager.FAULTY || stopping;

                            starting    = false;

                        }finally{

                            start_stop_mon.exit();
                        }

                        if ( stop_required ){

                            DiskManagerImpl.this.stop( false );
                        }
                    }
                });

        }finally{

            start_stop_mon.exit();
        }
    }

    private void
    startSupport()
    {
            //if the data file is already in the completed files dir, we want to use it
        boolean files_exist = false;

        if (download_manager.isPersistent()){
        	
        	/**
        	 * Try one of these candidate directories, see if the data already exists there.
        	 */
        	File[] move_to_dirs = DownloadManagerMoveHandler.getRelatedDirs(download_manager); 
        	
        	for (int i=0; i<move_to_dirs.length; i++) {
        		String move_to_dir = move_to_dirs[i].getAbsolutePath();
        		if (filesExist (move_to_dir)) {
                    alreadyMoved = files_exist = true;
                    download_manager.setTorrentSaveDir(move_to_dir);
                    break;
                }
        	}
        }

        reader.start();

        checker.start();

        writer.start();
        
        // If we haven't yet allocated the files, take this chance to determine
        // whether any relative paths should be taken into account for default
        // save path calculations.
        if (!alreadyMoved && !download_manager.isDataAlreadyAllocated()) {
        	
        	// Check the files don't already exist in their current location.
        	if (!files_exist) {files_exist = this.filesExist();}
        	if (!files_exist) {
        		SaveLocationChange transfer = 
        			DownloadManagerMoveHandler.onInitialisation(download_manager);
        		if (transfer != null) {
        			if (transfer.download_location != null || transfer.download_name != null) {
        				File dl_location = transfer.download_location;
        				if (dl_location == null) {dl_location = download_manager.getAbsoluteSaveLocation().getParentFile();}
        				if (transfer.download_name == null) {
        					download_manager.setTorrentSaveDir(dl_location.getAbsolutePath());
        				}
        				else {
        					download_manager.setTorrentSaveDir(dl_location.getAbsolutePath(), transfer.download_name);
        				}
        			}
        			if (transfer.torrent_location != null || transfer.torrent_name != null) {
        				try {download_manager.setTorrentFile(transfer.torrent_location, transfer.torrent_name);}
        				catch (DownloadManagerException e) {Debug.printStackTrace(e);}	
        			}
        		}
        	}
        }

            //allocate / check every file

        int[] alloc_result = allocateFiles();

        int	newFiles 		= alloc_result[0];
        int	notNeededFiles	= alloc_result[1];
        
        if ( getState() == FAULTY ){

                // bail out if broken in the meantime
                // state will be "faulty" if the allocation process is interrupted by a stop

            return;
        }

        if ( getState() == FAULTY  ){

                // bail out if broken in the meantime

            return;
        }

        setState( DiskManager.CHECKING );

        resume_handler.start();

        if ( checking_enabled ){
        	
	        if ( newFiles == 0 ){
	
	            resume_handler.checkAllPieces(false);
	
	            	// unlikely to need piece list, force discard
	            
	            if ( getRemainingExcludingDND() == 0 ){
	            	
	            	checkFreePieceList( true );
	            }
	        }else if ( newFiles + notNeededFiles != files.length ){
	
	                //  if not a fresh torrent, check pieces ignoring fast resume data
	
	            resume_handler.checkAllPieces(true);
	        }
        }
        
        if ( getState() == FAULTY  ){

            return;
        }

            // in all the above cases we want to continue to here if we have been "stopped" as
            // other components require that we end up either FAULTY or READY

            //3.Change State

        	// flag for an update of the 'downloaded' values for skipped files 
        
        skipped_file_set_changed = true;
        
        setState( READY );
    }

    public boolean
    stop(
    	boolean	closing )
    {
        try{
        	if ( move_in_progress ){
        		
        		Debug.out( "stop called while move in progress!" );
        	}
       	
            start_stop_mon.enter();

            if ( !started ){

                return( false );
            }

                // we need to be careful if we're still starting up as this may be
                // a re-entrant "stop" caused by a faulty state being reported during
                // startup. Defer the actual stop until starting is complete

            if ( starting ){

                stopping    = true;

                    // we can however safely stop things at this point - this is important
                    // to interrupt an alloc/recheck process that might be holding up the start
                    // operation

                checker.stop();

                writer.stop();

                reader.stop();

                resume_handler.stop( closing );

                	// at least save the current stats to download state  - they'll be persisted later
                	// when the "real" stop gets through
                
                saveState( false );
                
                return( true );
            }

            started     = false;

            stopping    = false;

        }finally{

            start_stop_mon.exit();
        }

        started_sem.reserve();

        checker.stop();

        writer.stop();

        reader.stop();

        resume_handler.stop( closing );

        if ( files != null ){

            for (int i = 0; i < files.length; i++){

                try{
                    if (files[i] != null) {

                        files[i].getCacheFile().close();
                    }
                }catch ( Throwable e ){

                    setFailed( "File close fails: " + Debug.getNestedExceptionMessage(e));
                }
            }
        }

        if ( getState() == DiskManager.READY ){

            try{

                saveResumeData( false );

            }catch( Exception e ){

                setFailed( "Resume data save fails: " + Debug.getNestedExceptionMessage(e));
            }
        }

        saveState();

        // can't be used after a stop so we might as well clear down the listeners
        listeners.clear();
        
        return( false );
    }

    public boolean 
    isStopped() 
    {
       	if ( move_in_progress ){
    		
    		Debug.out( "isStopped called while move in progress!" );
    	}
       	
        try{
            start_stop_mon.enter();
            
            return( !( started || starting || stopping ));
            
        }finally{

            start_stop_mon.exit();
        }
    }
    
    public boolean
    filesExist()
    {
        return( filesExist( download_manager.getAbsoluteSaveLocation().getParent()));
    }

    protected boolean
    filesExist(
        String  root_dir )
    {
        if ( !torrent.isSimpleTorrent()){

            root_dir += File.separator + download_manager.getAbsoluteSaveLocation().getName();
        }

        if ( !root_dir.endsWith( File.separator )){

            root_dir    += File.separator;
        }

        // System.out.println( "root dir = " + root_dir_file );

        DMPieceMapperFile[] pm_files = piece_mapper.getFiles();

        String[]    storage_types = getStorageTypes();

  		DownloadManagerState state = download_manager.getDownloadState();

        for (int i = 0; i < pm_files.length; i++) {

            DMPieceMapperFile pm_info = pm_files[i];

            File    relative_file = pm_info.getDataFile();

            long target_length = pm_info.getLength();

                // use the cache file to ascertain length in case the caching/writing algorithm
                // fiddles with the real length
                // Unfortunately we may be called here BEFORE the disk manager has been
                // started and hence BEFORE the file info has been setup...
                // Maybe one day we could allocate the file info earlier. However, if we do
                // this then we'll need to handle the "already moved" stuff too...

            DiskManagerFileInfoImpl file_info = pm_info.getFileInfo();

            boolean close_it    = false;

            try{
                if ( file_info == null ){

                    int storage_type = DiskManagerUtil.convertDMStorageTypeFromString( storage_types[i]);
            
                	file_info = createFileInfo( state, pm_info, i, root_dir, relative_file, storage_type );
                	
                	close_it = true;
                }

                try{
                    CacheFile   cache_file  = file_info.getCacheFile();
                    File        data_file   = file_info.getFile(true);

                    if ( !cache_file.exists()){

                            // look for something sensible to report

                          File current = data_file;

                          while( !current.exists()){

                            File    parent = current.getParentFile();

                            if ( parent == null ){

                                break;

                            }else if ( !parent.exists()){

                                current = parent;

                            }else{

                                if ( parent.isDirectory()){

                                    errorMessage = current.toString() + " not found.";

                                }else{

                                    errorMessage = parent.toString() + " is not a directory.";
                                }

                                return( false );
                            }
                          }

                          errorMessage = data_file.toString() + " not found.";

                          return false;
                    }

                        // only test for too big as if incremental creation selected
                        // then too small is OK

                    long    existing_length = file_info.getCacheFile().getLength();

                    if ( existing_length > target_length ){

                        if ( COConfigurationManager.getBooleanParameter("File.truncate.if.too.large")){

                            file_info.setAccessMode( DiskManagerFileInfo.WRITE );

                            file_info.getCacheFile().setLength( target_length );

                            Debug.out( "Existing data file length too large [" +existing_length+ ">" +target_length+ "]: " + data_file.getAbsolutePath() + ", truncating" );

                        }else{

                            errorMessage = "Existing data file length too large [" +existing_length+ ">" +target_length+ "]: " + data_file.getAbsolutePath();

                            return false;
                        }
                    }
                }finally{

                    if ( close_it ){

                        file_info.getCacheFile().close();
                    }
                }
            }catch( Throwable e ){

                errorMessage = Debug.getNestedExceptionMessage(e) + " (filesExist:" + relative_file.toString() + ")";

                return( false );
            }
        }

        return true;
    }

    private DiskManagerFileInfoImpl
    createFileInfo(
    	DownloadManagerState		state,
    	DMPieceMapperFile			pm_info,
    	int							file_index,
    	String						root_dir,
    	File						relative_file,
    	int							storage_type )
    
    	throws Exception
    {    	
        try{
        
            return( new DiskManagerFileInfoImpl(
                                this,
                                root_dir,
                                relative_file,
                                file_index,
                                pm_info.getTorrentFile(),
                                storage_type ));
            
        }catch( CacheFileManagerException e ){
        	
        		// unfortunately there are files out there with ascii < 32 chars in that are invalid on windows
        		// but ok on other file systems
        		// it would be possible to fix this in FileUtil.convertOSSPecificChars but my worry with this is that it
        		// would potentially break existing downloads, so I whimped out and decided to take the approach of
        		// detecting the issue and using file-links to work around it
        	
        	if ( Debug.getNestedExceptionMessage(e).contains( "volume label syntax is incorrect" )){
        		
              	File target_file = new File( root_dir + relative_file.toString());

        		File actual_file = state.getFileLink( file_index, target_file );
        		
        		if ( actual_file == null ){
        			
        			actual_file = target_file;
        		}
        		
        		File temp = actual_file;
        		
        		Stack<String>	comps = new Stack<String>();
        		
        		boolean	fixed = false;
        		
        		while( temp != null ){
        			
        			if ( temp.exists()){
        				
        				break;
        			}
        			
        			String old_name 	= temp.getName();
        			String new_name		= "";
        			
        			char[] chars = old_name.toCharArray();
        			
        			for ( char c: chars ){
        				
        				int	i_c = (int)c;
        				
        				if ( i_c >= 0 && i_c < 32 ){
        					
        					new_name += "_";
        					
        				}else{
        					
        					new_name += c;
        				}
        			}
        			
        			comps.push( new_name );
        			
        			if ( !old_name.equals( new_name )){
        				
        				fixed = true;
        			}
        			
        			temp = temp.getParentFile();
        		}
        		
        		if ( fixed ){
        			
        			while( !comps.isEmpty()){
        				
        				String comp = comps.pop();
        				
        				if ( comps.isEmpty()){
        				
        					String prefix = Base32.encode( new SHA1Simple().calculateHash( relative_file.toString().getBytes( "UTF-8" ))).substring( 0, 4 );
        					
        					comp = prefix + "_" + comp;
        				}
        				
        				temp = new File( temp, comp );
        			}
        			       			
           			Debug.outNoStack( "Fixing unsupported file path: " + actual_file.getAbsolutePath() + " -> " + temp.getAbsolutePath());
           			
           			state.setFileLink( file_index, target_file, temp );
           			
                    return(
                    	new DiskManagerFileInfoImpl(
                            this,
                            root_dir,
                            relative_file,
                            file_index,
                            pm_info.getTorrentFile(),
                            storage_type ));
        		}        		
        	}
        	       		
        	throw( e );
        }
    }

    private int[]
    allocateFiles()
    {
    	int[] fail_result = { -1, -1 };
    	
        Set file_set    = new HashSet();

        DMPieceMapperFile[] pm_files = piece_mapper.getFiles();

        DiskManagerFileInfoImpl[] allocated_files = new DiskManagerFileInfoImpl[pm_files.length];

        DownloadManagerState	state = download_manager.getDownloadState();
        
        try{
            allocation_scheduler.register( this );

            setState( ALLOCATING );

            allocated = 0;

            int numNewFiles 		= 0;
            int notRequiredFiles	= 0;
            
            String  root_dir = download_manager.getAbsoluteSaveLocation().getParent();

            if ( !torrent.isSimpleTorrent()){

                root_dir += File.separator + download_manager.getAbsoluteSaveLocation().getName();
            }

            root_dir    += File.separator;

            String[]    storage_types = getStorageTypes();

			String incomplete_suffix = state.getAttribute( DownloadManagerState.AT_INCOMP_FILE_SUFFIX );

            for ( int i=0;i<pm_files.length;i++ ){

            	if ( stopping ){
            		
                    this.errorMessage = "File allocation interrupted - download is stopping";

                    setState( FAULTY );

                    return( fail_result );
            	}
            	
                final DMPieceMapperFile pm_info = pm_files[i];

                final long target_length = pm_info.getLength();

                File relative_data_file = pm_info.getDataFile();

                DiskManagerFileInfoImpl fileInfo;

                try{
                    int storage_type = DiskManagerUtil.convertDMStorageTypeFromString( storage_types[i]);

                    fileInfo = createFileInfo( state, pm_info, i, root_dir, relative_data_file, storage_type );

                    allocated_files[i] = fileInfo;

                    pm_info.setFileInfo( fileInfo );

                }catch ( Exception e ){

                    this.errorMessage = Debug.getNestedExceptionMessage(e) + " (allocateFiles:" + relative_data_file.toString() + ")";

                    setState( FAULTY );

                    return( fail_result );
                }

                CacheFile   cache_file      = fileInfo.getCacheFile();
                File        data_file       = fileInfo.getFile(true);

                String  file_key = data_file.getAbsolutePath();

                if ( Constants.isWindows ){

                    file_key = file_key.toLowerCase();
                }

                if ( file_set.contains( file_key )){

                    this.errorMessage = "File occurs more than once in download: " + data_file.toString() + ".\nRename one of the files in Files view via the right-click menu.";

                    setState( FAULTY );

                    return( fail_result );
                }

                file_set.add( file_key );

                String      ext  = data_file.getName();

                if ( incomplete_suffix != null && ext.endsWith( incomplete_suffix )){
                	
                	ext = ext.substring( 0, ext.length() - incomplete_suffix.length());
                }
                
                int separator = ext.lastIndexOf(".");

                if ( separator == -1 ){

                    separator = 0;
                }

                fileInfo.setExtension(ext.substring(separator));

                    //Added for Feature Request
                    //[ 807483 ] Prioritize .nfo files in new torrents
                    //Implemented a more general way of dealing with it.

                String extensions = COConfigurationManager.getStringParameter("priorityExtensions","");

                if(!extensions.equals("")) {
                    boolean bIgnoreCase = COConfigurationManager.getBooleanParameter("priorityExtensionsIgnoreCase");
                    StringTokenizer st = new StringTokenizer(extensions,";");
                    while(st.hasMoreTokens()) {
                        String extension = st.nextToken();
                        extension = extension.trim();
                        if(!extension.startsWith("."))
                            extension = "." + extension;
                        boolean bHighPriority = (bIgnoreCase) ?
                                              fileInfo.getExtension().equalsIgnoreCase(extension) :
                                              fileInfo.getExtension().equals(extension);
                        if (bHighPriority)
                            fileInfo.setPriority(1);
                    }
                }

                fileInfo.setDownloaded(0);
                
                int st = cache_file.getStorageType();
                
                boolean compact = st == CacheFile.CT_COMPACT || st == CacheFile.CT_PIECE_REORDER_COMPACT;
                
                boolean mustExistOrAllocate = ( !compact ) || RDResumeHandler.fileMustExist(download_manager, fileInfo);
                
                	// delete compact files that do not contain pieces we need
                
                if (!mustExistOrAllocate && cache_file.exists()){
                	
					data_file.delete();
                }
                
                if ( cache_file.exists() ){

                    try {

                        //make sure the existing file length isn't too large

                        long    existing_length = fileInfo.getCacheFile().getLength();

                        if(  existing_length > target_length ){

                            if ( COConfigurationManager.getBooleanParameter("File.truncate.if.too.large")){

                                fileInfo.setAccessMode( DiskManagerFileInfo.WRITE );

                                cache_file.setLength( target_length );

                                fileInfo.setAccessMode( DiskManagerFileInfo.READ );
                                
                                Debug.out( "Existing data file length too large [" +existing_length+ ">" +target_length+ "]: " +data_file.getAbsolutePath() + ", truncating" );

                            }else{

                                this.errorMessage = "Existing data file length too large [" +existing_length+ ">" +target_length+ "]: " + data_file.getAbsolutePath();

                                setState( FAULTY );

                                return( fail_result );
                            }
                        }else if ( existing_length < target_length ){
                        	
                        	if ( !compact ){
	                        		// file is too small
	                        	
	                         	if ( !allocateFile( fileInfo, data_file, existing_length, target_length )){
	                            	
	                      			// aborted
	                    		
	                         		return( fail_result );
	                         	}
                        	}
                        }
                    }catch (Throwable e) {

                    	fileAllocFailed( data_file, target_length, false, e );
                    	
                        setState( FAULTY );

                        return( fail_result );
                    }

                    allocated += target_length;

                } else if ( mustExistOrAllocate ){  
                	
                		//we need to allocate it
                        //make sure it hasn't previously been allocated

                    if ( download_manager.isDataAlreadyAllocated() ){

                        this.errorMessage = "Data file missing: " + data_file.getAbsolutePath();

                        setState( FAULTY );

                        return( fail_result );
                    }

 
                    try{
 
                    	if ( !allocateFile( fileInfo, data_file, -1, target_length )){
                    	
                      			// aborted
                    		
                    		return( fail_result );
                    	}
                    	
                    }catch( Throwable e ){

                    	fileAllocFailed( data_file, target_length, true, e );

                        setState( FAULTY );

                        return( fail_result );
                    }

                    numNewFiles++;
                    
                }else{
                	
                	notRequiredFiles++;
                }
            }

                // make sure that "files" doens't become visible to the rest of the world until all
                // entries have been populated

            files   = allocated_files;
            fileset = new DiskManagerFileInfoSetImpl(files,this);

            loadFilePriorities();

            download_manager.setDataAlreadyAllocated( true );

            return( new int[]{ numNewFiles, notRequiredFiles });

        }finally{

            allocation_scheduler.unregister( this );

                // if we failed to do the allocation make sure we close all the files that
                // we might have opened

            if ( files == null ){

                for (int i=0;i<allocated_files.length;i++){

                    if ( allocated_files[i] != null ){

                        try{
                            allocated_files[i].getCacheFile().close();

                        }catch( Throwable e ){
                        }
                    }
                }
            }
        }
    }

    private boolean
    allocateFile(
    	DiskManagerFileInfoImpl		fileInfo,
    	File						data_file,
    	long						existing_length,	// -1 if not exists
    	long						target_length )
    
    	throws Throwable
    {
        while( started ){

            if ( allocation_scheduler.getPermission( this )){

                break;
            }
        }

        if ( !started ){

                // allocation interrupted

            return( false );
        }

        fileInfo.setAccessMode( DiskManagerFileInfo.WRITE );

        if ( COConfigurationManager.getBooleanParameter("Enable incremental file creation" )){

                //  do incremental stuff

        	if ( existing_length < 0 ){
            
        			// only do this if it doesn't exist
        		
        		fileInfo.getCacheFile().setLength( 0 );
        	}
        }else{
        
	            //fully allocate. XFS borks with zero length files though
	
	        if ( 	target_length > 0 && 
	        		!Constants.isWindows && 
	        		COConfigurationManager.getBooleanParameter("XFS Allocation") ){
	        	
	            fileInfo.getCacheFile().setLength( target_length );
	            
	            long	resvp_start;
	            long	resvp_len;
	            
	            if ( existing_length > 0 ){
	            	
	            	resvp_start = existing_length;
	            	resvp_len	= target_length - existing_length;
	            }else{
	            	resvp_start = 0;
	            	resvp_len	= target_length;
	            }
	            
	            String[] cmd = {"/usr/sbin/xfs_io","-c", "resvsp " + resvp_start + " " + resvp_len, data_file.getAbsolutePath()};
	            
	            ByteArrayOutputStream os = new ByteArrayOutputStream();
	            byte[] buffer = new byte[1024];
	            try {
	                Process p = Runtime.getRuntime().exec(cmd);
	                for (int count = p.getErrorStream().read(buffer); count > 0; count = p.getErrorStream().read(buffer)) {
	                   os.write(buffer, 0, count);
	                }
	                os.close();
	                p.waitFor();
	            } catch (IOException e) {
	            	String message = MessageText.getString("xfs.allocation.xfs_io.not.found", new String[] {e.getMessage()});
	            	Logger.log(new LogAlert(this, LogAlert.UNREPEATABLE, LogAlert.AT_ERROR, message));
	            }
	            if (os.size() > 0) {
	            	String message = os.toString().trim();
	            	if (message.endsWith("is not on an XFS filesystem")) {
	            		Logger.log(new LogEvent(this, LogIDs.DISK, "XFS file allocation impossible because \"" + data_file.getAbsolutePath()
	            				+ "\" is not on an XFS filesystem. Original error reported by xfs_io : \"" + message + "\""));
	            	} else {
	            		throw new Exception(message);
	            	}
	            }
	            
	            allocated += target_length;
	            
	        }else if( COConfigurationManager.getBooleanParameter("Zero New") ) {  //zero fill
	        	
	        	boolean successfulAlloc = false;
	
	        	try {
	        		successfulAlloc = writer.zeroFile( fileInfo, target_length );
	        		
	        	}catch( Throwable e ){
	        			// in case an error occured set the error message before we set it to FAULTY in the finally clause, the exception handler further down is too late
	        		
	        		fileAllocFailed( data_file, target_length, existing_length==-1, e );
	                
	                throw( e );
	                
	        	}finally{
	        		
	        		if (!successfulAlloc){
	        			
						try{
								// failed to zero it, delete it so it gets done next start
							
							fileInfo.getCacheFile().close();
							
							fileInfo.getCacheFile().delete();
							
						}catch (Throwable e){
							
						}
						
						setState(FAULTY);
					}
	        	}
	        	
	        		// the zeroFile method updates allocation as it occurs
	        	
	        }else{
	
	                //reserve the full file size with the OS file system
	
	            fileInfo.getCacheFile().setLength( target_length );
	            
	            allocated += target_length;
	        }
        }
        
        fileInfo.setAccessMode( DiskManagerFileInfo.READ );
        
        return( true );
    }
    
    private void
    fileAllocFailed(
    	File		file,
    	long		length,
    	boolean		is_new,
    	Throwable 	e )
    {
    	errorMessage = Debug.getNestedExceptionMessage(e) + " (allocateFiles " + (is_new?"new":"existing") + ":" + file.toString() + ")";
    	
    	if ( errorMessage.indexOf( "not enough space" ) != -1 ){
    		
    		errorType	= ET_INSUFFICIENT_SPACE;
    		
    		if ( length >= 4*1024*1024*1024L ){
    			
    				// might be FAT32 limit, see if we really have run out of space
    			
    			errorMessage = MessageText.getString( "DiskManager.error.nospace_fat32" );
    			
    		}else{
    			
    			errorMessage = MessageText.getString( "DiskManager.error.nospace" );
    		}
    	}
    }
    
    public DiskAccessController
    getDiskAccessController()
    {
        return( disk_access_controller );
    }

    public void
    enqueueReadRequest(
        DiskManagerReadRequest request,
        DiskManagerReadRequestListener listener )
    {
        reader.readBlock( request, listener );
    }

	public boolean
	hasOutstandingReadRequestForPiece(
		int		piece_number )
	{
		return( reader.hasOutstandingReadRequestForPiece( piece_number ));
	}
	
    public int
    getNbPieces()
    {
        return nbPieces;
    }

    public int
    getPercentDone()
    {
        return percentDone;
    }

    public void
    setPercentDone(
        int         num )
    {
        percentDone = num;
    }

    public long
    getRemaining() {
        return remaining;
    }

    private void
    fixupSkippedCalculation()
    {
        if ( skipped_file_set_changed ){

            DiskManagerFileInfoImpl[]   current_files = files;

            if ( current_files != null ){

                skipped_file_set_changed    = false;

                try{
                    file_piece_mon.enter();

                    long skipped   		= 0;
                    long downloaded  	= 0;

                    for (int i=0;i<current_files.length;i++){

                        DiskManagerFileInfoImpl file = current_files[i];

                        if ( file.isSkipped()){

                        	skipped   += file.getLength();
                        	downloaded  += file.getDownloaded();
                        }
                    }
                    
                    skipped_file_set_size 	= skipped;
                    skipped_but_downloaded	= downloaded;
                }finally{

                    file_piece_mon.exit();
                }
            
                DownloadManagerStats stats = download_manager.getStats();
            
                if (stats instanceof DownloadManagerStatsImpl) {
                	((DownloadManagerStatsImpl) stats).setSkippedFileStats(skipped_file_set_size, skipped_but_downloaded);
                }
            }
        }
    }

    public long
    getRemainingExcludingDND()
    {
    	fixupSkippedCalculation();

        long rem = ( remaining - ( skipped_file_set_size - skipped_but_downloaded ));

        if ( rem < 0 ){

            rem = 0;
        }

        return( rem );
    }
    
	public long getSizeExcludingDND() {
		fixupSkippedCalculation();

		return totalLength - skipped_file_set_size;
	}

	public int getPercentDoneExcludingDND() {
		long sizeExcludingDND = getSizeExcludingDND();
		if (sizeExcludingDND <= 0) {
			return 0;
		}
		float pct = (sizeExcludingDND - getRemainingExcludingDND()) / (float) sizeExcludingDND;
		return (int) (1000 * pct);
	}

    public long
    getAllocated()
    {
        return( allocated );
    }

    public void
    setAllocated(
        long        num )
    {
        allocated   = num;
    }

    /**
     *  Called when status has CHANGED and should only be called by DiskManagerPieceImpl
     */
    
    public void
    setPieceDone(
        DiskManagerPieceImpl    dmPiece,
        boolean                 done )
    {
        int piece_number =dmPiece.getPieceNumber();
        int piece_length =dmPiece.getLength();
        try
        {
            file_piece_mon.enter();

            if (dmPiece.isDone() != done )
            {
                dmPiece.setDoneSupport(done);

                if (done)
                    remaining -=piece_length;
                else
                    remaining +=piece_length;

                DMPieceList piece_list = getPieceList( piece_number );

                for (int i =0; i <piece_list.size(); i++)
                {

                    DMPieceMapEntry piece_map_entry =piece_list.get(i);

                    DiskManagerFileInfoImpl this_file =piece_map_entry.getFile();

                    long file_length =this_file.getLength();

                    long file_done =this_file.getDownloaded();

                    long file_done_before =file_done;

                    if (done)
                        file_done +=piece_map_entry.getLength();
                    else
                        file_done -=piece_map_entry.getLength();

                    if (file_done <0)
                    {
                        Debug.out("piece map entry length negative");

                        file_done =0;

                    } else if (file_done >file_length)
                    {
                        Debug.out("piece map entry length too large");

                        file_done =file_length;
                    }

                    if (this_file.isSkipped())
                    {
                        skipped_but_downloaded +=(file_done -file_done_before);
                    }

                    this_file.setDownloaded(file_done);

                    	// change file modes based on whether or not the file is complete or not
                    
                    if ( file_done == file_length ){
                         
                    	try{
                      		DownloadManagerState state = download_manager.getDownloadState();

                    		try{
	                    		
	                    		String suffix = state.getAttribute( DownloadManagerState.AT_INCOMP_FILE_SUFFIX );
	                    		
	                    		if ( suffix != null && suffix.length() > 0 ){
	                    		
									String prefix = state.getAttribute( DownloadManagerState.AT_DND_PREFIX );
									
									if ( prefix == null ){
										
										prefix = "";
									}

	                    			File base_file = this_file.getFile( false );
	                    			
	                    			int	file_index = this_file.getIndex();
	                    			
	                    			File link = state.getFileLink( file_index, base_file );
	                    			
	                    			if ( link != null ){
	                    				
	                    				String	name = link.getName();
	                    				
	                    				if ( name.endsWith( suffix ) && name.length() > suffix.length()){
	                    					
	                    					String	new_name = name.substring( 0, name.length() - suffix.length());
	                    					
	                    					if ( !this_file.isSkipped()){
	                    						
	                    							// retain prefix for dnd files as it is there to prevent clashes
	                    						
		                    					if ( prefix.length() > 0 && new_name.startsWith( prefix )){
		                    						
		                    						new_name = new_name.substring( prefix.length());
		                    					}
	                    					}
	                    					
	                    					File new_file = new File( link.getParentFile(), new_name );
	                    					
	                    					if ( !new_file.exists()){
	                    						
	                    						this_file.renameFile( new_name );
	                    						
	                    						if ( base_file.equals( new_file )){
	                    							
	                    							state.setFileLink( file_index, base_file, null );
	                    							
	                    						}else{
	                    							
	                    							state.setFileLink( file_index, base_file, new_file );
	                    						}
	                    					}
	                    				}
	                    			}else{
	                    				
	                    					/* bit nasty this but I (parg) spent a while trying to find an alternative solution to this and gave up
	                    					 * With simple torrents, if a 'file-move' operation is performed while incomplete with a suffix defined then
	                    					 * the actual save location gets updated and the link information lost as a result (it is as if the user went and
	                    					 * moved the file to another one that happened to end in the suffix). Detect this situation and do the best we
	                    					 * can to remove the auto-added suffix
	                    					 */
	                    				
	                    				if ( this_file.getTorrentFile().getTorrent().isSimpleTorrent()){
	                    			
	                    					File save_location = download_manager.getSaveLocation();
	                    					
	                    					String	name = save_location.getName();
		                    				
		                    				if ( name.endsWith( suffix ) && name.length() > suffix.length()){
		                    					
		                    					String	new_name = name.substring( 0, name.length() - suffix.length());
		                    					
		                    					if ( !this_file.isSkipped()){
		                    						
		                    							// retain prefix for dnd files as it is there to prevent clashes
		                    						
			                    					if ( prefix.length() > 0 && new_name.startsWith( prefix )){
			                    						
			                    						new_name = new_name.substring( prefix.length());
			                    					}
		                    					}
		                    					
		                    					File new_file = new File( save_location.getParentFile(), new_name );
		                    					
		                    					if ( !new_file.exists()){
		                    						
		                    						this_file.renameFile( new_name );
		                    						
		                    						if ( save_location.equals( new_file )){
		                    							
		                    							state.setFileLink( 0, save_location, null );
		                    							
		                    						}else{
		                    							
		                    							state.setFileLink( 0, save_location, new_file );
		                    						}
		                    					}
		                    				}
	                    				}	
	                    			}
	                    		}
                    		}finally{
                    			
                             	if ( this_file.getAccessMode() == DiskManagerFileInfo.WRITE ){

                               		this_file.setAccessMode( DiskManagerFileInfo.READ );
                               	}
                             	
                             		// only record completion during normal downloading, not rechecking etc
                             	
                             	if ( getState() == READY ){
                             	
                             		state.setLongParameter( DownloadManagerState.PARAM_DOWNLOAD_FILE_COMPLETED_TIME, SystemTime.getCurrentTime());
                             	}
                    		}
                        }catch ( Throwable e ){
                        
                            setFailed("Disk access error - " +Debug.getNestedExceptionMessage(e));

                            Debug.printStackTrace(e);
                        }

                        // note - we don't set the access mode to write if incomplete as we may
                        // be rechecking a file and during this process the "file_done" amount
                        // will not be file_length until the end. If the file is read-only then
                        // changing to write will cause trouble!
                    }
                }
                
                if ( getState() == READY ){
                
                		// don't start firing these until we're ready otherwise we send notifications
                		// for complete pieces during initialisation 
                	
                	listeners.dispatch(LDT_PIECE_DONE_CHANGED, dmPiece);
                }
            }
        } finally
        {
            file_piece_mon.exit();
        }

    }

    public void
    accessModeChanged(
        DiskManagerFileInfoImpl     file,
        int                         old_mode,
        int                         new_mode )
    {
        listeners.dispatch(
            LDT_ACCESS_MODE_CHANGED,
            new Object[]{ file, new Integer(old_mode), new Integer(new_mode)});
    }

    public DiskManagerPiece[] getPieces()
    {
        return pieces;
    }

    public DiskManagerPiece getPiece(int PieceNumber)
    {
        return pieces[PieceNumber];
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public int
    getPieceLength(
    	int		piece_number )
    {
		if (piece_number == nbPieces -1 ){
			
			return( lastPieceLength );
			
		}else{
			
			return( pieceLength );
		}
    }
    
    public long getTotalLength() {
        return totalLength;
    }

    public int getLastPieceLength() {
        return lastPieceLength;
    }

    public int getState() {
        return state_set_via_method;
    }

    protected void
    setState(
        int     _state )
    {
            // we never move from a faulty state

        if ( state_set_via_method == FAULTY ){

            if ( _state != FAULTY ){

                Debug.out( "DiskManager: attempt to move from faulty state to " + _state );
            }

            return;
        }

        if ( state_set_via_method != _state ){

            int params[] = {state_set_via_method, _state};

            state_set_via_method = _state;

            if ( _state == FAULTY ){
            	
            	if ( errorType == ET_NONE ){
            		
            		errorType	= ET_OTHER;
            	}
            }
            
            listeners.dispatch( LDT_STATECHANGED, params);
        }
    }


    public DiskManagerFileInfo[]
    getFiles()
    {
        return files;
    }
    
    public DiskManagerFileInfoSet getFileSet() {
    	return fileset;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
	public int
	getErrorType()
	{
		return( errorType );
	}
	
    public void
    setFailed(
        final String        reason )
    {
            /**
             * need to run this on a separate thread to avoid deadlock with the stopping
             * process - setFailed tends to be called from within the read/write activities
             * and stopping these requires this.
             */

        new AEThread("DiskManager:setFailed")
        {
            public void
            runSupport()
            {
                errorMessage    = reason;

                Logger.log(new LogAlert(DiskManagerImpl.this, LogAlert.UNREPEATABLE, LogAlert.AT_ERROR,
                            errorMessage));


                setState( DiskManager.FAULTY );

                DiskManagerImpl.this.stop( false );
            }
        }.start();
    }

    public void
    setFailed(
        final DiskManagerFileInfo       file,
        final String                    reason )
    {
            /**
             * need to run this on a separate thread to avoid deadlock with the stopping
             * process - setFailed tends to be called from within the read/write activities
             * and stopping these requires this.
             */

        new AEThread("DiskManager:setFailed")
        {
            public void
            runSupport()
            {
                errorMessage    = reason;

                Logger.log(new LogAlert(DiskManagerImpl.this, LogAlert.UNREPEATABLE, LogAlert.AT_ERROR,
                        errorMessage));


                setState( DiskManager.FAULTY );

                DiskManagerImpl.this.stop( false );

                RDResumeHandler.recheckFile( download_manager, file );
            }
        }.start();
    }

	public int
	getCacheMode()
	{
		return( CacheFileOwner.CACHE_MODE_NORMAL );
	}
	
	public long[]
	getReadStats()
	{
		if ( reader == null ){
	
			return( new long[]{ 0, 0 });
		}
				
		return( reader.getStats());
	}
	
	public DMPieceMap  
	getPieceMap()
	{
		DMPieceMap	map = piece_map_use_accessor;
		
		if ( map == null ){
				
			// System.out.println( "Creating piece list for " + new String( torrent.getName()));
			
			piece_map_use_accessor = map = piece_mapper.getPieceMap();			
		}
		
		piece_map_use_accessor_time = SystemTime.getCurrentTime();

		return( map );
	}
	
	public DMPieceList
	getPieceList(
		int	piece_number )
	{
		DMPieceMap	map = getPieceMap();

		return( map.getPieceList( piece_number ));
	}
		
	public void
	checkFreePieceList(
		boolean	force_discard )
	{
		if ( piece_map_use_accessor == null ){
			
			return;
		}
		
		long now = SystemTime.getCurrentTime();
		
		if ( !force_discard ){
			
			if ( now < piece_map_use_accessor_time ){
				
				piece_map_use_accessor_time	= now;
				
				return;
				
			}else if ( now - piece_map_use_accessor_time < DM_FREE_PIECELIST_TIMEOUT ){
					
				return;
			}
		}
		
		// System.out.println( "Discarding piece list for " + new String( torrent.getName()));
		
		piece_map_use_accessor = null;
	}
	
    public byte[]
    getPieceHash(
        int piece_number )

        throws TOTorrentException
    {
        return( torrent.getPieces()[ piece_number ]);
    }

    public DiskManagerReadRequest
    createReadRequest(
        int pieceNumber,
        int offset,
        int length )
    {
        return( reader.createReadRequest( pieceNumber, offset, length ));
    }

    public DiskManagerCheckRequest
    createCheckRequest(
        int     pieceNumber,
        Object  user_data )
    {
        return( checker.createCheckRequest( pieceNumber, user_data ));
    }

	public boolean
	hasOutstandingCheckRequestForPiece(
		int		piece_number )
	{
		return( checker.hasOutstandingCheckRequestForPiece( piece_number ));
	}
	
    public void
    enqueueCompleteRecheckRequest(
        DiskManagerCheckRequest             request,
        DiskManagerCheckRequestListener     listener )

    {
        checker.enqueueCompleteRecheckRequest( request, listener );
    }

    public void
    enqueueCheckRequest(
        DiskManagerCheckRequest         request,
        DiskManagerCheckRequestListener listener )
    {
        checker.enqueueCheckRequest( request, listener );
    }

    public int getCompleteRecheckStatus()
    {
      return ( checker.getCompleteRecheckStatus());
    }

    public int
    getMoveProgress()
    {
    	if ( move_in_progress ){
    		
    		return( move_progress );
    	}
    	
    	return( -1 );
    }
    
	public void
	setPieceCheckingEnabled(
		boolean		enabled )
	{
		checking_enabled = enabled;
		
		checker.setCheckingEnabled( enabled );
	}
	
    public DirectByteBuffer
    readBlock(
        int pieceNumber,
        int offset,
        int length )
    {
        return( reader.readBlock( pieceNumber, offset, length ));
    }

    public DiskManagerWriteRequest
    createWriteRequest(
        int                 pieceNumber,
        int                 offset,
        DirectByteBuffer    data,
        Object              user_data )
    {
        return( writer.createWriteRequest( pieceNumber, offset, data, user_data ));
    }

    public void
    enqueueWriteRequest(
        DiskManagerWriteRequest         request,
        DiskManagerWriteRequestListener listener )
    {
        writer.writeBlock( request, listener );
    }

	public boolean
	hasOutstandingWriteRequestForPiece(
		int		piece_number )
	{
		return( writer.hasOutstandingWriteRequestForPiece( piece_number ));
	}

    public boolean
    checkBlockConsistencyForWrite(
    	String				originator,
        int 				pieceNumber,
        int 				offset,
        DirectByteBuffer 	data )
    {
        if (pieceNumber < 0) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " pieceNumber=" + pieceNumber + " < 0"));
            return false;
        }
        if (pieceNumber >= this.nbPieces) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " pieceNumber=" + pieceNumber + " >= this.nbPieces="
                                + this.nbPieces));
            return false;
        }
        int length = this.pieceLength;
        if (pieceNumber == nbPieces - 1) {
            length = this.lastPieceLength;
        }
        if (offset < 0) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " offset=" + offset + " < 0"));
            return false;
        }
        if (offset > length) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " offset=" + offset + " > length=" + length));
            return false;
        }
        int size = data.remaining(DirectByteBuffer.SS_DW);
        if (size <= 0) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " size=" + size + " <= 0"));
            return false;
        }
        if (offset + size > length) {
            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR,
                        "Write invalid: " + originator + " offset=" + offset + " + size=" + size + " > length="
                                + length));
            return false;
        }
        return true;
    }
    
	public boolean
	checkBlockConsistencyForRead(
		String	originator,
	    boolean	peer_request,
	    int 	pieceNumber,
	    int 	offset,
	    int 	length )
	{
		return( DiskManagerUtil.checkBlockConsistencyForRead(this, originator, peer_request, pieceNumber, offset, length));
	}
	
	public boolean
	checkBlockConsistencyForHint(
		String	originator,
	    int 	pieceNumber,
	    int 	offset,
	    int 	length )
	{
		return( DiskManagerUtil.checkBlockConsistencyForHint(this, originator, pieceNumber, offset, length));
	}
	
    public void
    saveResumeData(
        boolean interim_save )

        throws Exception
    {
        resume_handler.saveResumeData( interim_save );
    }

    public void downloadEnded( OperationStatus op_status ) {
        moveDownloadFilesWhenEndedOrRemoved( false, true, op_status );
    }

    public void downloadRemoved () {
        moveDownloadFilesWhenEndedOrRemoved(true, true, null );
    }

    private boolean 
    moveDownloadFilesWhenEndedOrRemoved(
    	final boolean 			removing, 
    	final boolean 			torrent_file_exists,
    	final OperationStatus	op_status )
    {
      try {
        start_stop_mon.enter();
                
        final boolean ending = !removing; // Just a friendly alias.

        /**
         * It doesn't matter if we set alreadyMoved, but don't end up moving the files.
         * This is because we only get called once (when it matters), which is when the
         * download has finished. We only want this to apply when the download has finished,
         * not if the user restarts the (already completed) download.
         */
        if (ending) {
            if (this.alreadyMoved) {return false;}
            this.alreadyMoved = true;
        }

        SaveLocationChange move_details;
        if (removing) {
        	move_details = DownloadManagerMoveHandler.onRemoval(this.download_manager);
        	
        }else{
        	DownloadManagerMoveHandler.onCompletion(
        		this.download_manager,
        		new DownloadManagerMoveHandler.MoveCallback()
        		{
        			public void 
        			perform(
        				SaveLocationChange move_details ) 
        			{
        				moveFiles( move_details, true, op_status );
        			}
        		});
        	
        	move_details = null;
        }
        
        if ( move_details != null ){
        	
        	moveFiles( move_details, true, op_status );
        }
        
        return true;

      }
      finally{
    	      	  
          start_stop_mon.exit();
          
          if (!removing) {
              try{
                  saveResumeData(false);
              }catch( Throwable e ){
                  setFailed("Resume data save fails: " + Debug.getNestedExceptionMessage(e));
              }
          }

      }
    }
    
    public void 
    moveDataFiles(
    	File 				new_parent_dir, 
    	String 				new_name,
    	OperationStatus		op_status )
    {
    	SaveLocationChange loc_change = new SaveLocationChange();
    	
    	loc_change.download_location 	= new_parent_dir;
    	loc_change.download_name 		= new_name;
    	
    	moveFiles( loc_change, false, op_status );
    }

    protected void 
    moveFiles(
    	SaveLocationChange 	loc_change, 
    	boolean 			change_to_read_only,
    	OperationStatus		op_status )
    {
    	boolean move_files = false;
    	if (loc_change.hasDownloadChange()) {
    		move_files = !this.isFileDestinationIsItself(loc_change);
    	}
    	
        try {
            start_stop_mon.enter();
            
            /**
             * The 0 suffix is indicate that these are quite internal, and are
             * only intended for use within this method.
             */
            boolean files_moved = true;
            if (move_files) {
            	try{
            		move_progress		= 0;
            		move_in_progress 	= true;
            		
            		files_moved = moveDataFiles0(loc_change, change_to_read_only, op_status );
            		
            	}finally{
            		
            		move_in_progress = false;
            	}
            }

            if (loc_change.hasTorrentChange() && files_moved) {
                moveTorrentFile(loc_change);
            }
        }
        catch(Exception e) {
            Debug.printStackTrace(e);
        }
        finally{
        	  
        	start_stop_mon.exit();
    }
  }
    
  // Helper function
  private void logMoveFileError(String destination_path, String message) {
      Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR, message));
      Logger.logTextResource(new LogAlert(this, LogAlert.REPEATABLE,
                      LogAlert.AT_ERROR, "DiskManager.alert.movefilefails"),
                      new String[] {destination_path, message});
  }
  
  private boolean isFileDestinationIsItself(SaveLocationChange loc_change) {
	  File old_location = download_manager.getAbsoluteSaveLocation();
	  File new_location = loc_change.normaliseDownloadLocation(old_location);
	  try {
		  old_location = old_location.getCanonicalFile();
		  new_location = new_location.getCanonicalFile();
          if (old_location.equals(new_location)) {return true;}
          if (!download_manager.getTorrent().isSimpleTorrent() && FileUtil.isAncestorOf(new_location, old_location)) {
       		String msg = "Target is sub-directory of files";
       		logMoveFileError(new_location.toString(), msg);
       		return true;
          }
	  }          
	  catch (Throwable e) {
		  Debug.out(e);
	  }
	  return false;
  }
	  
    private boolean moveDataFiles0(SaveLocationChange loc_change, final boolean change_to_read_only, OperationStatus op_status ) throws Exception  {
    	
    		// there is a time race condition here between a piece being marked as complete and the
    		// associated file actions being taken (switch to read only, do the 'incomplete file suffix' nonsense)
    		// and the peer controller noting that the download is complete and kicking off these actions.
    		// in order to ensure that the completion actions are done prior to us running here we do:
    	
    	try{
            file_piece_mon.enter();
             
    	}finally{
    		
    		file_piece_mon.exit();
    	}
    	
    	File move_to_dir_name = loc_change.download_location;
    	if (move_to_dir_name == null) {move_to_dir_name = download_manager.getAbsoluteSaveLocation().getParentFile();}

    	final String move_to_dir = move_to_dir_name.toString();
    	final String new_name = loc_change.download_name;
    	
    		// consider the two cases:
    		//		simple torrent:  /temp/simple.avi
    		// 		complex torrent: /temp/complex[/other.avi]
    	
    		// we are moving the files to the "move_to_arg" /M and possibly renaming to "wibble.x"
    		//		/temp/simple.avi, null	->  /M/simple.avi
    		//		/temp, "wibble.x"		->	/M/wibble.x
    	
    		//		/temp/complex[/other.avi], null		->	/M/complex[/other.avi]
    		//		/temp, "wibble.x"					->	/M/wibble.x[/other.avi]
    	
   	
    	if ( files == null ){return false;}
    	
        if (isFileDestinationIsItself(loc_change)) {return false;}
        
        final boolean[]	got_there = { false };
               
        if ( op_status != null ){
        	
       		op_status.gonnaTakeAWhile( 
        		new GettingThere()
        		{
        			public boolean 
        			hasGotThere() 
        			{
        				synchronized( got_there ){
        					
        					return( got_there[0] );
        				}
        			}
        		});
        }
        
        try{        
	    	boolean simple_torrent = download_manager.getTorrent().isSimpleTorrent();
	    	
	    		// absolute save location does not follow links
	    		// 		for simple: /temp/simple.avi
	    		//		for complex: /temp/complex
	    	
	        final File save_location = download_manager.getAbsoluteSaveLocation();
	        
	        	// It is important that we are able to get the canonical form of the directory to
	        	// move to, because later code determining new file paths will break otherwise.
	 
	        final String move_from_name	= save_location.getName();
	        final String move_from_dir	= save_location.getParentFile().getCanonicalFile().getPath();        
	                  
	        final File[]    new_files   = new File[files.length];
	        
	        File[]    old_files   = new File[files.length];
	        boolean[] link_only   = new boolean[files.length];
	
	        long	total_bytes 		= 0;
	        
	        final long[]	file_lengths_to_move	 	= new long[files.length];
	        
	        for (int i=0; i < files.length; i++) {
	
	            File old_file = files[i].getFile(false);
		            
	            File linked_file = FMFileManagerFactory.getSingleton().getFileLink( torrent, i, old_file );
	
	            if ( !linked_file.equals(old_file)){
	
	                if ( simple_torrent ){
	
	                    // simple torrent, only handle a link if its a simple rename
	
	                    if ( linked_file.getParentFile().getCanonicalPath().equals( save_location.getParentFile().getCanonicalPath())){
	
	                        old_file  = linked_file;
	
	                    }else{
	
	                        link_only[i] = true;
	                    }
	                    
	                }else{
	                      // if we are linked to a file outside of the torrent's save directory then we don't
	                      // move the file
	
	                    if ( linked_file.getCanonicalPath().startsWith( save_location.getCanonicalPath())){
	
	                        old_file  = linked_file;
	
	                    }else{
	
	                        link_only[i] = true;
	                    }
	                }
	            }
	            
	            /**
	             * We are trying to calculate the relative path of the file within the original save
	             * directory, and then use that to calculate the new save path of the file in the new
	             * save directory.
	             * 
	             * We have three cases which we may deal with:
	             *   1) Where the file in the torrent has never been moved (therefore, old_file will
	             *      equals linked_file),
	             *   2) Where the file in the torrent has been moved somewhere elsewhere inside the save
	             *      path (old_file will not equal linked_file, but we will overwrite the value of
	             *      old_file with linked_file),
	             *   3) Where the file in the torrent has been moved outside of the download path - meaning
	             *      we set link_only[i] to true. This is just to update the internal reference of where
	             *      the file should be - it doesn't move the file at all.
	             *      
	             * Below, we will determine a new path for the file, but only in terms of where it should be
	             * inside the new download save location - if the file currently exists outside of the save
	             * location, we will not move it.
	             */
	            
	            old_files[i] = old_file;
	            
	            /**
	             * move_from_dir should be canonical (see earlier code).
	             * 
	             * Need to get canonical form of the old file, because that's what we are using for determining
	             * the relative path.
	             */ 
	            
	            String old_parent_path = old_file.getCanonicalFile().getParent();
	            
	            String sub_path;
	
	            /**
	             * Calculate the sub path of where the file lives compared to the new save location.
	             * 
	             * The code here has changed from what it used to be to fix bug 1636342:
	             *   https://sourceforge.net/tracker/?func=detail&atid=575154&aid=1636342&group_id=84122
	             */
	            
	            if ( old_parent_path.startsWith(move_from_dir)){
	            	
	            	sub_path = old_parent_path.substring(move_from_dir.length());
	            	
	            }else{
	            	
	            	logMoveFileError(move_to_dir, "Could not determine relative path for file - " + old_parent_path);
	            	
	            	throw new IOException("relative path assertion failed: move_from_dir=\"" + move_from_dir + "\", old_parent_path=\"" + old_parent_path + "\"");
	            }
	            
	              //create the destination dir
	            
	            if ( sub_path.startsWith( File.separator )){
	            	
	                sub_path = sub_path.substring(1);
	            }
	
	            	// We may be doing a rename, and if this is a simple torrent, we have to keep the names in sync.
	            
	            File new_file;
	            
	            if ( new_name == null ){
	            	
	            	new_file = new File( new File( move_to_dir, sub_path ), old_file.getName());
	            	
	            }else{
	            	
	            		// renaming
	            	
	            	if ( simple_torrent ){
	            		
	                   	new_file = new File( new File( move_to_dir, sub_path ), new_name );
	                    
	            	}else{
	            		
	            			// subpath includes the old dir name, replace this with new
	            		
	            		int	pos = sub_path.indexOf( File.separator );
	            		String	new_path;
	            		if (pos == -1) {
	            			new_path = new_name;
	            		}
	            		else {
	            			// Assertion check.
	            			String sub_sub_path = sub_path.substring(pos);
	            			String expected_old_name = sub_path.substring(0, pos);
	            			new_path = new_name + sub_sub_path;
	            			boolean assert_expected_old_name = expected_old_name.equals(save_location.getName());
	            			if (!assert_expected_old_name) {
	            				Debug.out("Assertion check for renaming file in multi-name torrent " + (assert_expected_old_name ? "passed" : "failed") + "\n" +
	            						"  Old parent path: " + old_parent_path + "\n" +
	            						"  Subpath: " + sub_path + "\n" +
	            						"  Sub-subpath: " + sub_sub_path + "\n" +
	            						"  Expected old name: " + expected_old_name + "\n" +
	            						"  Torrent pre-move name: " + save_location.getName() + "\n" +
	            						"  New torrent name: " + new_name + "\n" +
	            						"  Old file: " + old_file + "\n" +
	            						"  Linked file: " + linked_file + "\n" +
	            						"\n" +
	            						"  Move-to-dir: " + move_to_dir + "\n" +
	            						"  New path: " + new_path + "\n" +
	            						"  Old file [name]: " + old_file.getName() + "\n"
	            						);
	            			}
	            		}
	            			
	            		
	                   	new_file = new File( new File( move_to_dir, new_path ), old_file.getName());
	            	}
	            }
	
	            new_files[i]  = new_file;
	
	            if ( !link_only[i] ){
	
		            total_bytes += file_lengths_to_move[i] = old_file.length();

	                if ( new_file.exists()){
	
	                    String msg = "" + linked_file.getName() + " already exists in MoveTo destination dir";
	
	                    Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR, msg));
	
	                    Logger.logTextResource(new LogAlert(this, LogAlert.REPEATABLE,
	                              LogAlert.AT_ERROR, "DiskManager.alert.movefileexists"),
	                              new String[] { old_file.getName() });
	
	
	                    Debug.out(msg);
	
	                    return false;
	                }
	
	                FileUtil.mkdirs(new_file.getParentFile());
	            }
	        }
	        
	        String	abs_path = move_to_dir_name.getAbsolutePath();
	        
	        String	_average_config_key = null;
	        
	        try{
	        	_average_config_key = "dm.move.target.abps." + Base32.encode( abs_path.getBytes( "UTF-8" ));
	        	
	        }catch( Throwable e ){
	        	
	        	Debug.out(e );
	        }
	        
	        final String average_config_key	= _average_config_key;
	        
	        	// lazy here for rare case where all non-zero length files are links
	        
	        if ( total_bytes == 0 ){
	        	
	        	total_bytes = 1;
	        }
		        
	        long	done_bytes = 0;
	        
	        final Object	progress_lock = new Object();
	        final int[] 	current_file_index 	= { 0 };
	        final long[]	current_file_bs		= { 0 };
	        final long		f_total_bytes		= total_bytes;
	        
	        final long[]	last_progress_bytes		= { 0 };
	        final long[]	last_progress_update 	= { SystemTime.getMonotonousTime() };
	        
	        TimerEventPeriodic timer_event1 = 
	        	SimpleTimer.addPeriodicEvent(
	        		"MoveFile:speedster",
	        		1000,
	        		new TimerEventPerformer()
	        		{		
	        			private long	start_time = SystemTime.getMonotonousTime();
	        			
	        			private long	last_update_processed;
	        			
	        			private long	estimated_speed = 1*1024*1024;	// 1MB/sec default
	        				
	        			{
	        				if ( average_config_key != null ){
	        				
	        					long val = COConfigurationManager.getLongParameter( average_config_key, 0 );
	        					
	        					if ( val > 0 ){
	        						
	        						estimated_speed = val;
	        					}
	        				}
	        			}
	        			
	        			public void 
	        			perform(
	        				TimerEvent event )
	        			{
	        				synchronized( progress_lock ){
	        					
	        					int file_index = current_file_index[0];

	  		              		if ( file_index >= new_files.length ){

	  		              			return;
	  		              		}
	  		              			  		              		
	        					long 	now			= SystemTime.getMonotonousTime();
	        					
	        					long	last_update = last_progress_update[0];	        					
        						long	bytes_moved = last_progress_bytes[0];

	        					if ( last_update != last_update_processed ){
	        						
	        						last_update_processed = last_update;
	        					        						
	        						if ( bytes_moved > 10*1024*1024 ){
	        						
		        							// a usable amount of progress
		        						
		        						long	elapsed = now - start_time;
		        						
		        						estimated_speed = ( bytes_moved * 1000 ) / elapsed;
		        						
		        						// System.out.println( "estimated speed: " + estimated_speed );
	        						}
	        					}
	        					
	        					long	secs_since_last_update  = ( now - last_update ) / 1000;
	        					
	        					if ( secs_since_last_update > 2 ){
	        					
	        							// looks like we're not getting useful updates, add some in based on
	        							// elapsed time and average rate
	        						
	        						long	file_start_overall		= current_file_bs[0];
	        						long	file_end_overall 		= file_start_overall + file_lengths_to_move[ file_index ];
	        						long	bytes_of_file_remaining	= file_end_overall - bytes_moved;
	        						
	        						long	pretend_bytes = 0;
	        						
	        						long	current_speed	 	= estimated_speed;
	        						long	current_remaining	= bytes_of_file_remaining;
	        						long	current_added		= 0;
	        						
	        						int		percentage_to_slow_at	= 80;
	        						
	        						// System.out.println( "injection pretend progress" );
	        						
	        						for (int i=0;i<secs_since_last_update;i++){
	        								        							
	        							current_added += current_speed;
	        							pretend_bytes += current_speed;

	        							// System.out.println( "    pretend=" + pretend_bytes + ", rate=" + percentage_to_slow_at + ", speed=" + current_speed );
	        					
	        							if ( current_added > percentage_to_slow_at*current_remaining/100 ){
	        								
	        								percentage_to_slow_at = 50;
	        								
	        								current_speed = current_speed / 2;
	        								
	        								current_remaining = bytes_of_file_remaining - pretend_bytes;
	        								
	        								current_added = 0;
	        								
	        								if ( current_speed < 1024 ){
	        									
	        									current_speed = 1024;
	        								}
	        							}
	        								        							
	        							if ( pretend_bytes >= bytes_of_file_remaining ){
	        								
	        								pretend_bytes = bytes_of_file_remaining;
	        								
	        								break;
	        							}
	        						}
	        						
	        						long	pretend_bytes_moved = bytes_moved + pretend_bytes;
	        						
	  		              			move_progress = (int)( 1000*pretend_bytes_moved/f_total_bytes);
	  		              			
	  		              			// System.out.println( "pretend prog: " + move_progress );
	        					}
	        				}
	        			}
	        		});
	        
	        TimerEventPeriodic timer_event2 = 
	        	SimpleTimer.addPeriodicEvent(
	        		"MoveFile:observer",
	        		500,
	        		new TimerEventPerformer()
	        		{
	        			public void 
	        			perform(
	        				TimerEvent event )
	        			{
	        				int			index;
	        				File		file;
	        				
	  		              	synchronized( progress_lock ){
	  		            	  
	  		              		index = current_file_index[0];

	  		              		if ( index >= new_files.length ){

	  		              			return;
	  		              		}

	  		              			// unfortunately file.length() blocks on my NAS until the operation is complete :( 

	  		              		file = new_files[index];
	  		              	}
	  		              	
	  		              	long	file_length = file.length();
	  		              	
	  		              	synchronized( progress_lock ){
	  		              		
	  		              		if ( index == current_file_index[0]){
	  		              
	  		              			long	done_bytes = current_file_bs[0] + file_length;
	  		            		
	  		              			move_progress = (int)( 1000*done_bytes/f_total_bytes);
	  		              			
	  		              			last_progress_bytes[0]	= done_bytes;
	  		              			last_progress_update[0]	= SystemTime.getMonotonousTime();
	  		              		}
	  		              	} 
	        			}
	        		});

        	long	start = SystemTime.getMonotonousTime();

        	String	old_root_dir;
        	String	new_root_dir;
        	
        	if ( simple_torrent ){
        		
        		old_root_dir = move_from_dir;
        		new_root_dir = move_to_dir;
        		
        	}else{
        		
        		old_root_dir = move_from_dir + File.separator + move_from_name;
        		new_root_dir = move_to_dir + File.separator + (new_name==null?move_from_name:new_name ); 	
        	}
        	
	        try{
	        	
		        for (int i=0; i < files.length; i++){
		
		            File new_file = new_files[i];
		
		            try{
		
		              long initial_done_bytes = done_bytes;
		              		              
		              files[i].moveFile( new_root_dir, new_file, link_only[i] );
		
		              synchronized( progress_lock ){
		            	  
		            	  current_file_index[0] = i+1;

		            	  done_bytes = initial_done_bytes + file_lengths_to_move[i];
		            	  
		            	  current_file_bs[0] = done_bytes;
		            	  
			              move_progress = (int)( 1000*done_bytes/total_bytes);

			              last_progress_bytes[0]	= done_bytes;
		            	  last_progress_update[0]	= SystemTime.getMonotonousTime();
		              }
		              		              
		              if ( change_to_read_only ){
		
		                  files[i].setAccessMode(DiskManagerFileInfo.READ);
		              }
		
		            }catch( CacheFileManagerException e ){
		
		              String msg = "Failed to move " + old_files[i].toString() + " to destination " + new_root_dir + ": " + new_file + "/" + link_only[i];
		
		              Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR, msg));
		
		              Logger.logTextResource(new LogAlert(this, LogAlert.REPEATABLE,
		                              LogAlert.AT_ERROR, "DiskManager.alert.movefilefails"),
		                              new String[] { old_files[i].toString(),
		                                      Debug.getNestedExceptionMessage(e) });
		
		                  // try some recovery by moving any moved files back...
		
		              for (int j=0;j<i;j++){
		
		                  try{
		                      files[j].moveFile( old_root_dir, old_files[j],  link_only[j]);
		
		                  }catch( CacheFileManagerException f ){
		
		                      Logger.logTextResource(new LogAlert(this, LogAlert.REPEATABLE,
		                                      LogAlert.AT_ERROR,
		                                      "DiskManager.alert.movefilerecoveryfails"),
		                                      new String[] { old_files[j].toString(),
		                                              Debug.getNestedExceptionMessage(f) });
		
		                  }
		              }
		
		              return false;
		            }
		        }
	        }finally{
	        	
	        	timer_event1.cancel();
	        	timer_event2.cancel();
	        }
	        
	        long	elapsed_secs = ( SystemTime.getMonotonousTime() - start )/1000;
	        
	        if ( total_bytes > 10*1024*1024 && elapsed_secs > 10 ){
	        	
	        	long	bps = total_bytes / elapsed_secs;
	        	
	        	if ( average_config_key != null ){
    				
					COConfigurationManager.setParameter( average_config_key, bps );
	        	}
	        }
	        
	        //remove the old dir
	
	        if (  save_location.isDirectory()){
	
	        	TorrentUtils.recursiveEmptyDirDelete( save_location, false );
	        }
	
	        // NOTE: this operation FIXES up any file links
	
	        if ( new_name == null ){
	        	
	           	download_manager.setTorrentSaveDir( move_to_dir );
	
	        }else{
	        	
	        	download_manager.setTorrentSaveDir( move_to_dir, new_name );
	        }
	        
	        return true;
	        
        }finally{
 
        	synchronized( got_there ){
        		
        		got_there[0] = true;
        	}
        }
    }
    
    private void moveTorrentFile(SaveLocationChange loc_change) {
    	if (!loc_change.hasTorrentChange()) {return;}

		File old_torrent_file = new File(download_manager.getTorrentFileName());
		File new_torrent_file = loc_change.normaliseTorrentLocation(old_torrent_file);
		
		if (!old_torrent_file.exists()) {
            // torrent file's been removed in the meantime, just log a warning
            if (Logger.isEnabled())
                  Logger.log(new LogEvent(this, LOGID, LogEvent.LT_WARNING, "Torrent file '" + old_torrent_file.getPath() + "' has been deleted, move operation ignored" ));
            return;
		}
    	
    	try {download_manager.setTorrentFile(loc_change.torrent_location, loc_change.torrent_name);}
    	catch (DownloadManagerException e) {
            String msg = "Failed to move " + old_torrent_file.toString() + " to " + new_torrent_file.toString();

            if (Logger.isEnabled())
                Logger.log(new LogEvent(this, LOGID, LogEvent.LT_ERROR, msg));

            Logger.logTextResource(new LogAlert(this, LogAlert.REPEATABLE,
                            LogAlert.AT_ERROR, "DiskManager.alert.movefilefails"),
                            new String[] { old_torrent_file.toString(),
                                    new_torrent_file.toString() });

            Debug.out(msg);
       	}
    }

    public TOTorrent
    getTorrent()
    {
        return( torrent );
    }


    public void
    addListener(
        DiskManagerListener l )
    {
        listeners.addListener( l );

        int params[] = {getState(), getState()};

        listeners.dispatch( l, LDT_STATECHANGED, params);
    }

    public void
    removeListener(
        DiskManagerListener l )
    {
        listeners.removeListener(l);
    }
    
    public boolean
    hasListener(
    	DiskManagerListener	l )
    {
    	return( listeners.hasListener( l ));
    }

          /** Deletes all data files associated with torrent.
           * Currently, deletes all files, then tries to delete the path recursively
           * if the paths are empty.  An unexpected result may be that a empty
           * directory that the user created will be removed.
           *
           * TODO: only remove empty directories that are created for the torrent
           */

    public static void
    deleteDataFiles(
        TOTorrent   torrent,
        String      torrent_save_dir,       // enclosing dir, not for deletion
        String      torrent_save_file, 		// file or dir for torrent
        boolean		force_no_recycle )    
    {
        if (torrent == null || torrent_save_file == null ){

            return;
        }

        try{
            if (torrent.isSimpleTorrent()){

                File    target = new File( torrent_save_dir, torrent_save_file );

                target = FMFileManagerFactory.getSingleton().getFileLink( torrent, 0, target.getCanonicalFile());

                FileUtil.deleteWithRecycle( target, force_no_recycle );

            }else{

                PlatformManager mgr = PlatformManagerFactory.getPlatformManager();
                if( Constants.isOSX &&
                      torrent_save_file.length() > 0 &&
                      COConfigurationManager.getBooleanParameter("Move Deleted Data To Recycle Bin" ) &&
                		(! force_no_recycle ) &&
                      mgr.hasCapability(PlatformManagerCapabilities.RecoverableFileDelete) ) {

                    try
                    {
                        String  dir = torrent_save_dir + File.separatorChar + torrent_save_file + File.separatorChar;

                            // only delete the dir if there's only this torrent's files in it!

                        int numDataFiles = countDataFiles( torrent, torrent_save_dir, torrent_save_file );
                        if ( countFiles( new File(dir), numDataFiles) == numDataFiles){

                            mgr.performRecoverableFileDelete( dir );

                        }else{

                            deleteDataFileContents( torrent, torrent_save_dir, torrent_save_file, force_no_recycle );
                    }
                    }
                    catch(PlatformManagerException ex)
                    {
                        deleteDataFileContents( torrent, torrent_save_dir, torrent_save_file, force_no_recycle );
                    }
                }
                else{
                    deleteDataFileContents(torrent, torrent_save_dir, torrent_save_file, force_no_recycle);
                }

            }
        }catch( Throwable e ){

            Debug.printStackTrace( e );
        }
    }

    private static int
    countFiles(
        File    f, 
        int stopAfterCount )
    {
        if ( f.isFile()){

            return( 1 );
        }else{

            int res = 0;

            File[]  files = f.listFiles();

            if ( files != null ){

                for (int i=0;i<files.length;i++){

                    res += countFiles( files[i], stopAfterCount );
                    
                    if (res > stopAfterCount) {
                    	break;
                    }
                }
            }

            return( res );
        }
    }

    private static int
    countDataFiles(
        TOTorrent torrent,
        String torrent_save_dir,
        String torrent_save_file )
    {
        try{
            int res = 0;

            LocaleUtilDecoder locale_decoder = LocaleTorrentUtil.getTorrentEncoding( torrent );

            TOTorrentFile[] files = torrent.getFiles();

            for (int i=0;i<files.length;i++){

                byte[][]path_comps = files[i].getPathComponents();

                String  path_str = torrent_save_dir + File.separator + torrent_save_file + File.separator;

                for (int j=0;j<path_comps.length;j++){

                    String comp = locale_decoder.decodeString( path_comps[j] );

                    comp = FileUtil.convertOSSpecificChars( comp, j != path_comps.length-1 );

                    path_str += (j==0?"":File.separator) + comp;
                }

                File file = new File(path_str).getCanonicalFile();

                File linked_file = FMFileManagerFactory.getSingleton().getFileLink( torrent, i, file );

                boolean skip = false;

                if ( linked_file != file ){

                    if ( !linked_file.getCanonicalPath().startsWith(new File( torrent_save_dir ).getCanonicalPath())){

                        skip = true;
                    }
                }

                if ( !skip && file.exists() && !file.isDirectory()){

                    res++;
                }
            }

            return( res );

        }catch( Throwable e ){

            Debug.printStackTrace(e);

            return( -1 );
        }
    }

    private static void
    deleteDataFileContents(
        TOTorrent torrent,
        String torrent_save_dir,
        String 		torrent_save_file,
        boolean		force_no_recycle )

            throws TOTorrentException, UnsupportedEncodingException, LocaleUtilEncodingException
    {
        LocaleUtilDecoder locale_decoder = LocaleTorrentUtil.getTorrentEncoding( torrent );

        TOTorrentFile[] files = torrent.getFiles();

        String  root_path = torrent_save_dir + File.separator + torrent_save_file + File.separator;
        
        boolean delete_if_not_in_dir = COConfigurationManager.getBooleanParameter("File.delete.include_files_outside_save_dir");

        // delete all files, then empty directories

        for (int i=0;i<files.length;i++){

            byte[][]path_comps = files[i].getPathComponents();

            String  path_str    = root_path;

            for (int j=0;j<path_comps.length;j++){

                try{

                    String comp = locale_decoder.decodeString( path_comps[j] );

                    comp = FileUtil.convertOSSpecificChars( comp, j != path_comps.length-1 );

                    path_str += (j==0?"":File.separator) + comp;

                }catch( UnsupportedEncodingException e ){

                    Debug.out( "file - unsupported encoding!!!!");
                }
            }

            File file = new File(path_str);

            File linked_file = FMFileManagerFactory.getSingleton().getFileLink( torrent, i, file );

            boolean delete;

            if ( linked_file == file ){

                delete  = true;

            }else{

                    // only consider linked files for deletion if they are in the torrent save dir
                    // i.e. a rename probably instead of a retarget to an existing file elsewhere
                    // delete_if_not_in_dir does allow this behaviour to be overridden though.

                try{
                    if ( delete_if_not_in_dir || linked_file.getCanonicalPath().startsWith(new File( root_path ).getCanonicalPath())){

                        file    = linked_file;

                        delete  = true;

                    }else{

                        delete = false;
                    }
                }catch( Throwable e ){

                    Debug.printStackTrace(e);

                    delete = false;
                }
            }

            if ( delete && file.exists() && !file.isDirectory()){

                try{
                    FileUtil.deleteWithRecycle( file, force_no_recycle );

                }catch (Exception e){

                    Debug.out(e.toString());
                }
            }
        }

        TorrentUtils.recursiveEmptyDirDelete(new File( torrent_save_dir, torrent_save_file ));
    }

    public void
    skippedFileSetChanged(
        DiskManagerFileInfo file )
    {
        skipped_file_set_changed    = true;
        if ( priority_change_marker.incrementAndGet() == 0 ){
        	priority_change_marker.incrementAndGet();
        }
        listeners.dispatch(LDT_PRIOCHANGED, file);
    }

    public void
    priorityChanged(
        DiskManagerFileInfo file )
    {
        if ( priority_change_marker.incrementAndGet() == 0 ){
        	priority_change_marker.incrementAndGet();
        }
        listeners.dispatch(LDT_PRIOCHANGED, file);
    }

  private void
  loadFilePriorities()
  {
      DiskManagerUtil.loadFilePriorities( download_manager, fileset );
  }

  protected void
  storeFilePriorities()
  {
      storeFilePriorities( download_manager, files );
  }

  protected static void
  storeFilePriorities(
    DownloadManager         download_manager,
    DiskManagerFileInfo[]   files )
  {
	  DiskManagerUtil.storeFilePriorities ( download_manager, files );
  }

  protected static void
  storeFileDownloaded(
    DownloadManager         download_manager,
    DiskManagerFileInfo[]   files,
    boolean					persist )
  {
      DownloadManagerState  state = download_manager.getDownloadState();

      Map   details = new HashMap();

      List  downloaded = new ArrayList();

      details.put( "downloaded", downloaded );

      for (int i=0;i<files.length;i++){

          downloaded.add( new Long( files[i].getDownloaded()));
      }

      state.setMapAttribute( DownloadManagerState.AT_FILE_DOWNLOADED, details );

      if ( persist ){
    	  
    	  state.save();
      }
  }

  public void
  saveState()
  {
	  saveState( true );
  }
  
  protected void
  saveState(
	boolean	persist )
  {
      if ( files != null ){

        storeFileDownloaded( download_manager, files, persist );

        storeFilePriorities();
    }
      
      checkFreePieceList( false );
  }

  public DownloadManager getDownloadManager() {
    return download_manager;
  }

    public String
    getInternalName()
    {
        return( download_manager.getInternalName());
    }

    public DownloadManagerState
    getDownloadState()
    {
        return( download_manager.getDownloadState());
    }

    public File
    getSaveLocation()
    {
        return( download_manager.getSaveLocation());
    }

    public String[]
    getStorageTypes()
    {
        return( getStorageTypes( download_manager ));
    }
    
    public String getStorageType(int fileIndex) {
    	return( getStorageType( download_manager , fileIndex));
    }

    	// Used by DownloadManagerImpl too.
    
    public static String[] getStorageTypes(DownloadManager download_manager) {
        DownloadManagerState state = download_manager.getDownloadState();
        String[] types = state.getListAttribute(DownloadManagerState.AT_FILE_STORE_TYPES);
        if (types == null || types.length == 0) {
        	TOTorrentFile[] files = download_manager.getTorrent().getFiles();
            types = new String[download_manager.getTorrent().getFiles().length];
            
         	if ( reorder_storage_mode ){
        		
        		int	existing = state.getIntAttribute( DownloadManagerState.AT_REORDER_MIN_MB );
        		
        		if ( existing < 0 ){
        			
        			existing = reorder_storage_mode_min_mb;
        			
        			state.setIntAttribute( DownloadManagerState.AT_REORDER_MIN_MB, existing );
        		}
                  		
        		for (int i=0; i<types.length; i++){
                           		
            		if ( files[i].getLength()/(1024*1024) >= existing ){
            			            			
            			types[i] = "R";
            			
            		}else{
            			
            			types[i] = "L";
            		}
            	}
          	}else{
         		
         		for (int i=0; i<types.length; i++){
         			
         			types[i] = "L";
         		}
            }
         	
			state.setListAttribute(DownloadManagerState.AT_FILE_STORE_TYPES, types );
        }
        
        return( types );
    }
    
    	// Used by DownloadManagerImpl too.
    
    public static String getStorageType(DownloadManager download_manager, int fileIndex) {
        DownloadManagerState state = download_manager.getDownloadState();
        String type = state.getListAttribute(DownloadManagerState.AT_FILE_STORE_TYPES,fileIndex);
        
        if ( type != null ){
        	
        	return( type );
        }
        
        return( getStorageTypes( download_manager )[fileIndex]);
    }
    
    public static void
    setFileLinks(
        DownloadManager         download_manager,
        LinkFileMap    			links )
    {
        try{
            CacheFileManagerFactory.getSingleton().setFileLinks( download_manager.getTorrent(), links );

        }catch( Throwable e ){

            Debug.printStackTrace(e);
        }
    }

    /* (non-Javadoc)
     * @see org.gudy.azureus2.core3.logging.LogRelation#getLogRelationText()
     */
    public String getRelationText() {
        return "TorrentDM: '" + download_manager.getDisplayName() + "'";
    }


    /* (non-Javadoc)
     * @see org.gudy.azureus2.core3.logging.LogRelation#queryForClass(java.lang.Class)
     */
    public Object[] getQueryableInterfaces() {
        return new Object[] { download_manager, torrent };
    }

    public DiskManagerRecheckScheduler
    getRecheckScheduler()
    {
        return( recheck_scheduler );
    }

    public boolean isInteresting(int pieceNumber)
    {
        return pieces[pieceNumber].isInteresting();
    }

    public boolean isDone(int pieceNumber)
    {
        return pieces[pieceNumber].isDone();
    }

	public long
	getPriorityChangeMarker()
	{
		return( priority_change_marker.get());
	}
	
	public void
	generateEvidence(
		IndentWriter		writer )
	{
		writer.println( "Disk Manager" );
		
		try{
			writer.indent();
			
			writer.println( "percent_done=" + percentDone +",allocated=" + allocated+",remaining="+ remaining);
			writer.println( "skipped_file_set_size=" + skipped_file_set_size + ",skipped_but_downloaded=" + skipped_but_downloaded );
			writer.println( "already_moved=" + alreadyMoved );
		}finally{
			
			writer.exdent();
		}
	}

}
