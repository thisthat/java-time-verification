package testing.ManualChecks;

import IntermediateModelHelper.indexing.GenerateMethodSyncCallList;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.SyncMethodCall;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	List<ASTClass> classes = new ArrayList<>();
	static final String name = "activemq";

	public static void main(String[] args) throws Exception {

		MongoOptions.getInstance().setDbName(name);

		new Main().run1();
	}


	public void run() throws Exception {
		String f =  Main.class.getClassLoader().getResource("manual/HTTPNetworkConnection.java").getFile();
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(4);

		//add the second method
		f =  Main.class.getClassLoader().getResource("manual/HTTPNetworkConnection.java").getFile();
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(4);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("pendingRequest",
				Arrays.asList("BTRequest","httpRequest")
		));
		p.addClass(c1, c1.getMethodBySignature("pendingRequest",
				Arrays.asList("BTRequest","httpRequest")
		));
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

		String f = "/Users/giovanni/repository/sources/activemq/activemq-camel/src/main/java/org/apache/activemq/camel/CamelConnectionFactory.java";
		//Main.class.getClassLoader().getResource("activemq/QueueStorePrefetch.java").getFile();
		Java2AST a = new Java2AST(f,  true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		//p.addClass(c, c.getFirstMethodByName("getProducerBrokerExchange"));

		List<IASTMethod> list = new ArrayList<IASTMethod>();
		list.add(c.getFirstMethodByName("createActiveMQConnection"));
		GenerateMethodSyncCallList syncCalls = new GenerateMethodSyncCallList(c, list );//c.getMethods());
		List<SyncMethodCall> calls = syncCalls.calculateSyncCallList();
		/*,
				Arrays.asList("ThreadPool")
		));*/

		p.addClass(c, c.getFirstMethodByName("getProducerBrokerExchange"));
		/*
				Arrays.asList("ThreadPool")
		));*/
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		writer.write(graph.toGraphViz(false));
		writer.close();
	}
}
