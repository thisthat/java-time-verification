package firstEvalPCFG;

import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexSyncnumber {
	MongoConnector db;
	PrintWriter writer;

	public static void main(String[] args) throws Exception {
		new IndexSyncnumber().run();
	}

	private void run() throws Exception {
		conf();
		String base_path = IndexSyncnumber.class.getClassLoader().getResource("vuze/index.txt").getPath();
		base_path = base_path.substring(0, base_path.lastIndexOf("/") + 1);
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		double max = files.size();
		double n = 0;
		while (i.hasNext()) {
			n += 1;
			String filename = ((File)i.next()).getAbsolutePath();
			System.out.println(String.format("[%3.2f%%] Analyzing %s", (n/max)*100  ,filename));
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
			for(ASTClass c : v.listOfClasses){
				IndexingFile indexing = new IndexingFile(db);
				IndexData data = indexing.index(c);
				int sync_block = 0;//data.getListOfSyncBlocks().size();
				int sync_method = data.getListOfSyncMethods().size();
				int time_constraint = ApplyHeuristics.getConstraint(c).size();
				int sum = sync_block + sync_method + time_constraint;
				int imported = db.getClassesThatImports(c.getPackageName(), c.getName()).size();
				writer.println(String.format("%s;%s;%s;%s;%s;%s;%s", data.getClassPackage(), data.getName(), sync_block, sync_method, time_constraint, sum, imported));
				writer.flush();
			}
		}
		writer.close();
	}

	private void conf() throws Exception {
		this.db = MongoConnector.getInstance("test");
		this.db.drop();
		writer = new PrintWriter("result" + this.getClass().getSimpleName().toString() + ".csv", "UTF-8");
		writer.println("package;classname;sync-block;sync-method;time-constraint;sum;num-imported");
	}
}
