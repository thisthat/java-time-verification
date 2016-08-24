package PCFG;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;

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
		new Main().run();
	}

	public void run() throws IOException, ParseErrorsException {

		IM2PCFG p = new IM2PCFG();

		//first method
		String f =  Main.class.getClassLoader().getResource("RelatedContentSearcher.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		String method = "RelatedContentSearcher";
		p.addClass(c, method);

		//add the second method
		f =  Main.class.getClassLoader().getResource("RelatedContentSearcher.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast);
		ast.accept(v);
		c = v.listOfClasses.get(0);
		p.addClass(c, "cancel");

		// build
		PCFG graph = p.buildPCFG();
		System.out.println(graph.toGraphViz(false));
	}

	public void run2() throws Exception {
		IM2PCFG p = new IM2PCFG();

		//first method
		String f =  Main.class.getClassLoader().getResource("bugs/Thread_1.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		String method = "run";
		p.addClass(c, method);

		//add the second method
		f =  Main.class.getClassLoader().getResource("bugs/Thread_2.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast);
		ast.accept(v);
		c = v.listOfClasses.get(0);
		p.addClass(c, method);

		// build
		PCFG g = p.buildPCFG();

		System.out.println(g.toGraphViz(false));
	}

	public void run1() throws IOException, ParseErrorsException {



		//first method
		String f =  Main.class.getClassLoader().getResource("RelatedContentSearcher.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);

		classes.addAll(v.listOfClasses);

		//add the second method
		f =  Main.class.getClassLoader().getResource("VuzeActivitiesEntry.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast);
		ast.accept(v);

		classes.addAll(v.listOfClasses);

		for(ASTClass cOut : classes){
			for(ASTClass cIn : classes){
				for(IASTMethod mOut : cOut.getMethods()){
					for(IASTMethod mIn : cIn.getMethods()){
						System.out.println(mOut.getName() + "--" + mIn.getName());
						IM2PCFG p = new IM2PCFG();
						p.addClass(cOut, mOut.getName());
						p.addClass(cIn, mIn.getName());
						PCFG graph = p.buildPCFG();
						graph.optimize();
					}
				}
			}
		}




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