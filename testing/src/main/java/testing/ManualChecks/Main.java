package testing.ManualChecks;

import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
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
	static final String name = "wildfly-core";

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

		String f = "/Users/giovanni/repository/sources/wildfly-core/server/src/main/java/org/jboss/as/server/deployment/DeploymentUnitPhaseService.java";
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
		p.addClass(c, c.getFirstMethodByName("start"));
		/*,
				Arrays.asList("ThreadPool")
		));*/

		p.addClass(c, c.getFirstMethodByName("start"));
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
