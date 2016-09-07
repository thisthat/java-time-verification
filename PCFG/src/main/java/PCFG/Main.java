package PCFG;

import PCFG.converter.ToDot;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	List<ASTClass> classes = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		new Main().run2();
	}

	public void run() throws IOException, ParseErrorsException {

		IM2PCFG p = new IM2PCFG();

		//first method
		String f =  Main.class.getClassLoader().getResource("RelatedContentSearcher.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		String method = "RelatedContentSearcher";
		p.addClass(c, method, true);

		//add the second method
		f =  Main.class.getClassLoader().getResource("RelatedContentSearcher.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		c = v.listOfClasses.get(0);
		p.addClass(c, "cancel", true);

		// build
		PCFG graph = p.buildPCFG();
		ToDot toGraphViz = new ToDot(false);
		System.out.println(toGraphViz.convert(graph));
	}

	public void run2() throws Exception {
		String f =  Main.class.getClassLoader().getResource("bugs/Timer.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		f =  Main.class.getClassLoader().getResource("bugs/LightWeightSeed.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, "destroy");
		p.addClass(c1, "getDescription");
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		ToDot toGraphViz = new ToDot(false);
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}

	public void run1() throws IOException, ParseErrorsException {



		//first method
		String f =  Main.class.getClassLoader().getResource("smallSubscriptionManager.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);

		classes.addAll(v.listOfClasses);

		ASTClass c = classes.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, "preInitialise");
		p.addClass(c, "initWithCore");
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		ToDot toGraphViz = new ToDot(false);
		writer.write(toGraphViz.convert(graph));
		writer.close();





		//we have only one class
		/*ASTClass c = v.listOfClasses.get(0);
		String method = "run";
		p.addClass(c, method);


		c = v.listOfClasses.get(0);
		p.addClass(c, method);

		// build
		PCFG graph = p.buildPCFG();
		System.out.println(graph.toGraphViz(false));
		*/
	}
}
