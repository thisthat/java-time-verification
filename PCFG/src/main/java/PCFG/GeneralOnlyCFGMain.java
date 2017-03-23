package PCFG;


import PCFG.converter.IConverter;
import PCFG.converter.ToXAL;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GeneralOnlyCFGMain {

	List<ASTClass> classes = new ArrayList<>();
	static final String db_name = "coseacaso";

	public static void main(String[] args) throws Exception {
		MongoOptions.getInstance().setDbName(db_name);
		GeneralOnlyCFGMain m = new GeneralOnlyCFGMain();
		m.run();
	}



	public void run() throws Exception {
		String f =  "/Users/giovanni/repository/sources/progs/oMPC.java";
		String base = f.substring(0, f.lastIndexOf("/"));
		List<ASTClass> classes = JDTVisitor.parse(f, base);
		ASTClass c = classes.get(0);
		IM2CFG p = new IM2CFG();
		p.addClass(c, c.getMethods().get(1));
		PCFG graph = p.buildPCFG();
		graph.optimize();
		graph.optimize(new OptimizeTimeAutomata());


		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter("graph.xal"));
		IConverter toGraphViz = new ToXAL(c);
		writer.write(toGraphViz.convert(graph));
		writer.close();
	}



}
