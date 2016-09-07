package testing.ManualChecks;

import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	List<ASTClass> classes = new ArrayList<>();

	public static void main(String[] args) throws Exception {

		MongoOptions.getInstance().setDbName("vuze_top_file");

		new Main().run();
	}


	public void run() throws Exception {
		String f =  Main.class.getClassLoader().getResource("manual/ThreadPool.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		classes.addAll(v.listOfClasses);
		ASTClass c = classes.get(0);

		//add the second method
		f =  Main.class.getClassLoader().getResource("manual/NetworkGlueLoopBack.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, "releaseManual");
		p.addClass(c1, "NetworkGlueLoopBack");
		PCFG graph = p.buildPCFG();
		graph.optimize();

		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.dot"));
		writer.write(graph.toGraphViz(false));
		writer.close();
	}
}
