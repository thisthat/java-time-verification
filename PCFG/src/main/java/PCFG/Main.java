package PCFG;

import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.converter.IConverter;
import PCFG.converter.ToDot;
import PCFG.converter.ToXAL;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTMethod;
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
	static final String db_name = "jetty";

	public static void main(String[] args) throws Exception {
		MongoOptions.getInstance().setDbName(db_name);
		new Main().run();
	}



	public void run() throws Exception {
		String f =  Main.class.getClassLoader().getResource("Thread_1.java").getFile();
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c);
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.xal"));
		IConverter toGraphViz = new ToXAL();
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}

	public void run1() throws Exception {
		String f =  "/Users/giovanni/repository/sources/jetty/jetty-websocket/javax-websocket-client-impl/src/main/java/org/eclipse/jetty/websocket/jsr356/ClientContainer.java";
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		f =  "/Users/giovanni/repository/sources/jetty/jetty-websocket/websocket-client/src/main/java/org/eclipse/jetty/websocket/client/WebSocketClient.java";
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c);
		p.addClass(c1);
		PCFG graph = p.buildPCFG();
		graph.optimize();

		//BufferedWriter writer = null;
		//writer = new BufferedWriter(new FileWriter("graph.dot"));
		//IConverter toGraphViz = new ToDot(true);
		//writer.write(toGraphViz.convert(graph));
		//writer.close();
	}

	public void example_paper() throws Exception {
		MongoOptions.getInstance().setDbName("vuze");
		String f =  "/Users/giovanni/repository/sources/vuze/src/main/java/com/aelitis/azureus/core/impl/AzureusCoreImpl.java";
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		f =  "/Users/giovanni/repository/sources/vuze/src/main/java/com/aelitis/azureus/ui/common/table/impl/TableViewImpl.java";
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getFirstMethodByName("canStart"));
		p.addClass(c1, c1.getFirstMethodByName("runForSelectedRows"));
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph_example_1.dot"));
		IConverter toGraphViz = new ToDot(true);
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}

	public void example_paper2() throws Exception {
		MongoOptions.getInstance().setDbName("jetty");
		String f =  "/Users/giovanni/repository/sources/jetty/jetty-websocket/javax-websocket-client-impl/src/main/java/org/eclipse/jetty/websocket/jsr356/ClientContainer.java";
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		f =  "/Users/giovanni/repository/sources/jetty/jetty-websocket/websocket-client/src/main/java/org/eclipse/jetty/websocket/client/WebSocketClient.java";
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		int max = 0;
		PCFG g = null;
		for(IASTMethod m0 : c.getAllMethods()){
			for(IASTMethod m1 : c1.getAllMethods()){
				IM2PCFG p = new IM2PCFG();
				p.addClass(c, m0);
				p.addClass(c1, m1);
				PCFG graph = p.buildPCFG();
				if(graph.getESync().size() > max){
					g = graph;
					max = graph.getESync().size();
				}
			}
		}


		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph_example_2.dot"));
		IConverter toGraphViz = new ToDot(true);
		writer.write(toGraphViz.convert(g));
		writer.close();
	}


}
