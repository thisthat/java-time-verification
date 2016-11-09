package PCFG;

import PCFG.converter.IConverter;
import PCFG.converter.ToXAL;
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
	static final String db_name = "test_paper";

	public static void main(String[] args) throws Exception {
		//MongoOptions.getInstance().setDbName(db_name);
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

		//add the second method
		f =  Main.class.getClassLoader().getResource("Thread_2.java").getFile();
		a = new Java2AST(f, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("example1",
				Arrays.asList()
		));
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.xal"));
		IConverter toGraphViz = new ToXAL();
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}


}
