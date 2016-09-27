package testing.ManualChecks;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.IndexingSyncBlock;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import PCFG.structure.PCFG;
import PCFG.creation.IM2PCFG;
import PCFG.structure.edge.SyncEdge;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	List<ASTClass> classes = new ArrayList<>();
	static final String name = "manual";

	public static void main(String[] args) throws Exception {

		MongoOptions.getInstance().setDbName(name);

		new Main().run1();
	}


	public void run() throws Exception {
		String f =  Main.class.getClassLoader().getResource("manual/HTTPNetworkConnection.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(4);

		//add the second method
		f =  Main.class.getClassLoader().getResource("manual/HTTPNetworkConnection.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(4);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("pendingRequest",
				Arrays.asList("BTRequest","httpRequest")
		), false);
		p.addClass(c1, c1.getMethodBySignature("pendingRequest",
				Arrays.asList("BTRequest","httpRequest")
		), false);
		/*p.addClass(c1, c1.getMethodBySignature("NetworkGlueLoopBack",
				Arrays.asList("NetworkGlueListener")
		));*/
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		writer.write(graph.toGraphViz(false));
		writer.close();
	}

	public void run1() throws Exception {

		/*
		String path = "/Users/giovanni/repository/sources/activemq";
		double start = new Date().getTime();
		IndexingProject indexing = new IndexingProject(name);
		indexing.delete();
		System.out.println("Finish delete");
		indexing.indexProject(path, false);
		System.out.println("Indexing done");
		indexing.indexSyncBlock(path, false);
		System.out.println("Indexing Blocks done");
		indexing.indexSyncCall(path, false);
		System.out.println("Indexing Calls done");
		double end = new Date().getTime();
		System.out.println("[Indexing] "+ (end-start)/1000 + " s");
*/

		String f = "/Users/giovanni/repository/sources/vuze/vuze_mvn/src/main/java/com/aelitis/azureus/core/impl/AzureusCoreImpl.java";
		//Main.class.getClassLoader().getResource("activemq/QueueStorePrefetch.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("AzureusCoreImpl",
				Arrays.asList()
		));

		p.addClass(c1, c1.getMethodBySignature("AzureusCoreImpl",
				Arrays.asList()
		));
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		writer.write(graph.toGraphViz(false));
		writer.close();
	}
}
