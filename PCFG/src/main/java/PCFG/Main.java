package PCFG;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {



	public static void main(String[] args) throws IOException, ParseErrorsException {
		new Main().run();
	}

	public void run() throws IOException, ParseErrorsException {

		IM2PCFG p = new IM2PCFG();

		//first method
		String f =  Main.class.getClassLoader().getResource("Thread_1.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		String method = "run";
		p.addClass(c, method);

		//add the second method
		f =  Main.class.getClassLoader().getResource("Thread_2.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast);
		ast.accept(v);
		c = v.listOfClasses.get(0);
		p.addClass(c, method);

		// build
		PCFG graph = p.buildPCFG();
		System.out.println(graph.toGraphViz(false));
	}
}
