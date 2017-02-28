package testing.PCFGToXAL;

import PCFG.converter.ToXAL;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import XAL.XALStructure.items.XALDocument;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main2XAL {
	List<ASTClass> classes= new ArrayList<>();

	public static void main(String[] args) throws IOException {
		if(args.length < 2){
			System.out.println("Call the program passing as input a java file and the name of method to convert");
			return;
		}
		new Main2XAL().run(args[0], args[1]);
	}

	public void run(String f, String n) throws IOException {
		Java2AST a = new Java2AST(f, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		IM2PCFG p = new IM2PCFG();
		IASTMethod m = null;
		for(IASTMethod mm : c.getMethods()){
			if(mm.getName().equals(n)){
				m = mm;
			}
		}
		p.addClass(c, m);
		/*p.addClass(c1, c1.getMethodBySignature("initialisePlugins",
				Arrays.asList()
		));*/
		/*p.addClass(c1, c1.getMethodBySignature("NetworkGlueLoopBack",
				Arrays.asList("NetworkGlueListener")
		));*
		PCFG graph = p.buildPCFG();
		graph.optimize();
		ToXAL converter = new ToXAL();
		XALDocument d = converter.getXAL(graph,"test.xal");
		d.toFile();*/
	}
}
