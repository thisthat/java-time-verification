package IntermediateModelHelper.indexing;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The following class analyze a project and stores in MongoDB the indexing of it.
 * It creates a database per project. If a class name already exists in the database is not added.
 * Two classes are equals if they share the same name and packaging.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingProject {
	MongoConnector db;
	String projectName;
	boolean skipTest = true;

	/**
	 * Construct the db given the project name.
	 * @param name	Project Name
	 */
	public IndexingProject(String name) {
		this.db = MongoConnector.getInstance(name);
		this.projectName = name;
	}

	/**
	 * Construct the db given the standard project name defined in {@link MongoOptions}.
	 */
	public IndexingProject() {
		String name = MongoOptions.getInstance().getDbName();
		this.db = MongoConnector.getInstance(name);
		this.projectName = name;
	}

	public boolean isSkipTest() {
		return skipTest;
	}

	public void setSkipTest(boolean skipTest) {
		this.skipTest = skipTest;
	}

	public String getProjectName() {
		return projectName;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexProject(String base_path) {
		return indexProject(base_path, false);
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @param deleteOld	If true delete the database so we have fresh data.
	 * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexProject(String base_path, boolean deleteOld){
		//remove old data
		if(deleteOld) delete();

		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(this.skipTest && filename.contains("/test")){
				continue;
			}
			Java2AST a = null;
			try {
				a = new Java2AST(filename, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IndexingFile indexing = new IndexingFile(db);
				indexing.index(c);
				//db.add(index);
			}
			n_file++;
		}
		//ensure indexes
		db.ensureIndexes();
		return n_file;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexSyncCall(String base_path, boolean deleteOld){
		if(deleteOld) delete();
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(this.skipTest && filename.contains("/test")){
				continue;
			}
			/*System.out.println(filename);
			if(!filename.endsWith("/Airavata.java")){
				continue;
			}*/
			Java2AST a = null;
			try {
				a = new Java2AST(filename, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IndexingSyncCalls indexing = new IndexingSyncCalls(db);
				indexing.index(c);
				//db.add(index);
			}
			n_file++;
		}
		//ensure indexes
		db.ensureIndexes();
		return n_file;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexSyncBlock(String base_path, boolean deleteOld){
		if(deleteOld) delete();
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(this.skipTest && filename.contains("/test")){
				continue;
			}
			Java2AST a = null;
			try {
				a = new Java2AST(filename, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IndexingSyncBlock indexing = new IndexingSyncBlock(db);
				indexing.index(c);
				//db.add(index);
			}
			n_file++;
		}
		//ensure indexes
		db.ensureIndexes();
		return n_file;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @param delete	If true delete the database so we have fresh data.
	 * @return			A {@link Future} that will contain soon or late the number of file parsed. <u>IT WILL NOT RETURN THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public Future<Integer> asyncIndexProject(String base_path, boolean delete){
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> task = () -> indexProject(base_path, delete);
		return executor.submit(task);
	}
	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return			A {@link Future} that will contain soon or late the number of file parsed. <u>IT WILL NOT RETURN THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public Future<Integer> asyncIndexProject(String base_path){
		return asyncIndexProject(base_path, false);
	}


	public void delete(){
		db.getDb().drop();
	}

}
