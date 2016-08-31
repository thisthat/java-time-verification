/*
 * Created on Jul 11, 2008
 * Created by Paul Gardner
 *
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package com.aelitis.azureus.core.subs.impl;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyPair;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.config.ParameterListener;
import org.gudy.azureus2.core3.download.DownloadManagerState;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.utils.Utilities;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.ddb.DistributedDatabase;
import org.gudy.azureus2.plugins.download.*;
import org.gudy.azureus2.plugins.peers.PeerManager;
import org.gudy.azureus2.plugins.torrent.Torrent;
import org.gudy.azureus2.plugins.torrent.TorrentAttribute;
import org.gudy.azureus2.plugins.torrent.TorrentManager;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerEvent;
import org.gudy.azureus2.plugins.utils.DelayedTask;
import org.gudy.azureus2.plugins.utils.StaticUtilities;
import org.gudy.azureus2.plugins.utils.search.SearchException;
import org.gudy.azureus2.plugins.utils.search.SearchInstance;
import org.gudy.azureus2.plugins.utils.search.SearchObserver;
import org.gudy.azureus2.plugins.utils.search.SearchProvider;
import org.gudy.azureus2.plugins.utils.search.SearchResult;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;
import org.gudy.azureus2.pluginsimpl.local.torrent.TorrentImpl;
import org.gudy.azureus2.pluginsimpl.local.utils.UtilitiesImpl;
import org.gudy.bouncycastle.util.encoders.Base64;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreRunningListener;
import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.custom.Customization;
import com.aelitis.azureus.core.custom.CustomizationManager;
import com.aelitis.azureus.core.custom.CustomizationManagerFactory;
import com.aelitis.azureus.core.lws.LightWeightSeed;
import com.aelitis.azureus.core.lws.LightWeightSeedManager;
import com.aelitis.azureus.core.messenger.config.PlatformSubscriptionsMessenger;
import com.aelitis.azureus.core.metasearch.Engine;
import com.aelitis.azureus.core.metasearch.MetaSearchListener;
import com.aelitis.azureus.core.metasearch.MetaSearchManagerFactory;
import com.aelitis.azureus.core.metasearch.impl.web.WebEngine;
import com.aelitis.azureus.core.metasearch.impl.web.rss.RSSEngine;
import com.aelitis.azureus.core.security.CryptoECCUtils;
import com.aelitis.azureus.core.subs.*;
import com.aelitis.azureus.core.subs.SubscriptionUtils.SubscriptionDownloadDetails;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagManagerFactory;
import com.aelitis.azureus.core.torrent.PlatformTorrentUtils;
import com.aelitis.azureus.core.util.CopyOnWriteList;
import com.aelitis.azureus.core.vuzefile.*;
import com.aelitis.azureus.plugins.dht.*;
import com.aelitis.azureus.plugins.magnet.MagnetPlugin;
import com.aelitis.azureus.plugins.magnet.MagnetPluginProgressListener;
import com.aelitis.azureus.plugins.net.buddy.BuddyPluginUtils;
import com.aelitis.azureus.util.ImportExportUtils;
import com.aelitis.azureus.util.UrlFilter;
import com.aelitis.net.magneturi.MagnetURIHandler;


public class
SubscriptionManagerImpl
		implements SubscriptionManager, AEDiagnosticsEvidenceGenerator
{
	private static final String	CONFIG_FILE = "subscriptions.config";
	private static final String	LOGGER_NAME = "Subscriptions";

	private static final String CONFIG_MAX_RESULTS 			= "subscriptions.max.non.deleted.results";
	private static final String CONFIG_AUTO_START_DLS 		= "subscriptions.auto.start.downloads";
	private static final String CONFIG_AUTO_START_MIN_MB 	= "subscriptions.auto.start.min.mb";
	private static final String CONFIG_AUTO_START_MAX_MB 	= "subscriptions.auto.start.max.mb";
	private static final String CONFIG_AUTO_MARK_READ	 	= "subscriptions.auto.dl.mark.read.days";

	private static final String	CONFIG_RSS_ENABLE			= "subscriptions.config.rss_enable";

	private static final String	CONFIG_ENABLE_SEARCH			= "subscriptions.config.search_enable";

	private static final String	CONFIG_HIDE_SEARCH_TEMPLATES	= "subscriptions.config.hide_search_templates";

	private static final String	CONFIG_DL_SUBS_ENABLE		= "subscriptions.config.dl_subs_enable";
	private static final String	CONFIG_DL_RATE_LIMITS		= "subscriptions.config.rate_limits";

	private static final int DELETE_UNUSED_AFTER_MILLIS = 2*7*24*60*60*1000;


	private static SubscriptionManagerImpl		singleton;
	private static boolean						pre_initialised;

	private static final int random_seed = RandomUtils.nextInt( 256 );

	public static void
	preInitialise()
	{
		synchronized( SubscriptionManagerImpl.class ){

			if ( pre_initialised ){

				return;
			}

			pre_initialised = true;
		}
/*
		VuzeFileHandler.getSingleton().addProcessor(
				new VuzeFileProcessor()
				{
					public void
					process(
							VuzeFile[]		files,
							int				expected_types )
					{
						for (int i=0;i<files.length;i++){

							VuzeFile	vf = files[i];

							VuzeFileComponent[] comps = vf.getComponents();

							for (int j=0;j<comps.length;j++){

								VuzeFileComponent comp = comps[j];

								int	type = comp.getType();

								if ( 	type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION ||
										type == VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON ){

									try{
										Subscription subs = ((SubscriptionManagerImpl)getSingleton( false )).importSubscription(
												type,
												comp.getContent(),
												( expected_types &
														( VuzeFileComponent.COMP_TYPE_SUBSCRIPTION | VuzeFileComponent.COMP_TYPE_SUBSCRIPTION_SINGLETON )) == 0 );

										comp.setProcessed();

										comp.setData( Subscription.VUZE_FILE_COMPONENT_SUBSCRIPTION_KEY, subs );

									}catch( Throwable e ){

										Debug.printStackTrace(e);
									}
								}
							}
						}
					}
				});*/
		int i = 10;
	}


	private boolean		started;

	private static final int	TIMER_PERIOD		= 30*1000;

	private static final int	ASSOC_CHECK_PERIOD	= 5*60*1000;
	private static final int	ASSOC_CHECK_TICKS	= ASSOC_CHECK_PERIOD/TIMER_PERIOD;

	private static final int	CHAT_CHECK_PERIOD	= 3*60*1000;
	private static final int	CHAT_CHECK_TICKS	= CHAT_CHECK_PERIOD/TIMER_PERIOD;

	private static final int	SERVER_PUB_CHECK_PERIOD	= 10*60*1000;
	private static final int	SERVER_PUB_CHECK_TICKS	= SERVER_PUB_CHECK_PERIOD/TIMER_PERIOD;

	private static final int	TIDY_POT_ASSOC_PERIOD	= 30*60*1000;
	private static final int	TIDY_POT_ASSOC_TICKS	= TIDY_POT_ASSOC_PERIOD/TIMER_PERIOD;

	private static final int	SET_SELECTED_PERIOD		= 23*60*60*1000;
	private static final int	SET_SELECTED_FIRST_TICK	= 3*60*1000 /TIMER_PERIOD;
	private static final int	SET_SELECTED_TICKS		= SET_SELECTED_PERIOD/TIMER_PERIOD;

	private static final Object	SP_LAST_ATTEMPTED	= new Object();
	private static final Object	SP_CONSEC_FAIL		= new Object();

	private AzureusCore		azureus_core;

	private volatile DHTPluginInterface	dht_plugin_public;

	private List<SubscriptionImpl>		subscriptions	= new ArrayList<SubscriptionImpl>();

	private boolean	config_dirty;

	private static final int PUB_ASSOC_CONC_MAX				= 3;
	private static final int PUB_SLEEPING_ASSOC_CONC_MAX	= 1;

	private int		publish_associations_active;
	private boolean	publish_next_asyc_pending;

	private boolean publish_subscription_active;

	private TorrentAttribute		ta_subs_download;
	private TorrentAttribute		ta_subs_download_rd;
	private TorrentAttribute		ta_subscription_info;
	private TorrentAttribute		ta_category;
	private TorrentAttribute		ta_networks;

	private boolean					periodic_lookup_in_progress;
	private int						priority_lookup_pending;

	private CopyOnWriteList<SubscriptionManagerListener>			listeners = new CopyOnWriteList<SubscriptionManagerListener>();

	private SubscriptionSchedulerImpl	scheduler;

	private List<Object[]>				potential_associations	= new ArrayList<Object[]>();
	private Map<HashWrapper,Object[]>	potential_associations2	= new HashMap<HashWrapper,Object[]>();

	private boolean					meta_search_listener_added;

	private Pattern					exclusion_pattern = Pattern.compile( "azdev[0-9]+\\.azureus\\.com" );

	private SubscriptionRSSFeed		rss_publisher;

	private AEDiagnosticsLogger		logger;

	protected void
	initWithCore(
			AzureusCore 	_core )
	{
		COConfigurationManager.addParameterListener(
				CONFIG_MAX_RESULTS,
				new ParameterListener()
				{
					public void
					parameterChanged(
							String	 name )
					{
						final int	max_results = COConfigurationManager.getIntParameter( CONFIG_MAX_RESULTS );

						new AEThread2( "Subs:max results changer", true )
						{
							public void
							run()
							{
								checkMaxResults( max_results );
							}
						}.start();
					}
				});

		synchronized( this ){

			if ( started ){

				return;
			}

			started	= true;
		}
	}
}
