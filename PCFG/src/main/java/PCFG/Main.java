package PCFG;

import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.converter.IConverter;
import PCFG.converter.ToXAL;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	List<ASTClass> classes = new ArrayList<>();
	static final String db_name = "test_paper";

	public static void main(String[] args) throws Exception {
		//MongoOptions.getInstance().setDbName(db_name);
		String folder = args[0];
		new Main().run(folder);
	}


	public void run(String path) throws Exception {
		String _NAME_ = path.substring( path.lastIndexOf("/") +1 );
		MongoOptions.getInstance().setDbName(_NAME_);
		double start = new Date().getTime();
		IndexingProject indexing = new IndexingProject(_NAME_);
		indexing.delete();
		System.out.println("[Indexing] Started");
		indexing.indexProject(path, false);
		indexing.indexSyncBlock(path, false);
		indexing.indexSyncCall(path, false);
		double end = new Date().getTime();
		System.out.println("[Indexing] Finish: "+ (end-start)/1000 + " s");

		start = new Date().getTime();
		System.out.println("[XAL] Creation Started");

		File dir = new File(path);
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
			System.out.println("[DEBUG] Processing file: " + filename);
			Java2AST a = null;
			try {
				a = new Java2AST(filename, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			String fName = filename.substring( filename.lastIndexOf("/") +1 );
			fName = fName.substring(0 , fName.indexOf("."));
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				IM2PCFG p = new IM2PCFG();
				for(IASTMethod m : c.getMethods()){
					p.addClass(c, m);
				}
				// build
				PCFG graph = p.buildPCFG();
				BufferedWriter writer = null;
				if(v.listOfClasses.size() > 1)
					writer = new BufferedWriter(new FileWriter( fName + "_" + c.getName() + ".xal"));
				else
					writer = new BufferedWriter(new FileWriter( c.getName() + ".xal"));
				IConverter toGraphViz = new ToXAL();
				writer.write(toGraphViz.convert(graph));
				writer.close();
			}
			n_file++;
		}
		end = new Date().getTime();
		System.out.println("[XAL] Creation Ended: " + (end-start)/1000 + " s");

	}


}
