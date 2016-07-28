package IntermediateModelHelper.indexing;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import com.fasterxml.jackson.databind.ObjectMapper;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingProject {
	MongoConnector db;
	String projectName;

	public IndexingProject(String name) {
		this.db = MongoConnector.getInstance(name);
		this.projectName = name;
	}

	public String getProjectName() {
		return projectName;
	}

	public int indexProject(String base_path) {
		return indexProject(base_path, false);
	}

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
			Java2AST a = null;
			try {
				a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (ParseErrorsException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IndexingFile indexing = new IndexingFile();
				IndexData index = indexing.index(c);
				db.add(index);
			}
			n_file++;
		}
		return n_file;
	}

	public Future<Integer> asyncIndexProject(String base_path, boolean delete){
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<Integer> task = () -> indexProject(base_path, delete);
		return executor.submit(task);
	}
	public Future<Integer> asyncIndexProject(String base_path){
		return asyncIndexProject(base_path, false);
	}


	private void delete(){
		db.getDb().drop();
	}

}
