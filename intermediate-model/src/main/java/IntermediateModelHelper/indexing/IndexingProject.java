package IntermediateModelHelper.indexing;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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

	boolean skipTest = true;

	public boolean isSkipTest() {
		return skipTest;
	}

	public void setSkipTest(boolean skipTest) {
		this.skipTest = skipTest;
	}

	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public int indexProject(String base_path){
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
			List<ASTClass> result = JDTVisitor.parse(filename);
			//pp filename
			for(ASTClass c : result){
				IndexingFile indexing = new IndexingFile();
				indexing.index(c);
				//db.add(index);
			}
			n_file++;
		}
		return n_file;
	}


	/**
	 * Start the indexing from the <b>base_path</b> passed as parameter.
	 * It iterates on the directory and sub-directories searching for Java files.
	 * @param base_path	Path from where start to search for Java files.
	 * @return			A {@link Future} that will contain soon or late the number of file parsed. <u>IT WILL NOT RETURN THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
	 */
	public Future<Integer> asyncIndexProject(String base_path){
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> task = () -> indexProject(base_path);
		return executor.submit(task);
	}




}
