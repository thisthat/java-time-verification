package examples;

import PCFG.converter.IConverter;
import PCFG.converter.ToUppaal;
import PCFG.converter.ToXAL;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by giovanni on 02/05/2017.
 */
public class ExtractModelAndCnst {

    public static void main(String[] args) throws Exception {
        String f =  "/Users/giovanni/repository/java-xal/code-instrumentation/src/test/java/UndefinedTimeBehaviour.java";
        ASTClass c = JDTVisitor.parse(f,System.getProperty("user.dir")).get(0);
        IASTMethod m = c.getAllMethods().get(0);
        IM2CFG p = new IM2CFG();
        p.addClass(c,m);
        PCFG graph = p.buildPCFG();
        graph.optimize();
        graph.optimize(new OptimizeTimeAutomata());

        BufferedWriter writer = null;
        //writer = new BufferedWriter(new FileWriter("graph.xal"));
        writer = new BufferedWriter(new FileWriter("graph.xml"));
        IConverter toGraphViz = new ToXAL(c);
        IConverter toUppaal = new ToUppaal(ToUppaal.NAMING.LINE);
        //writer.write(toGraphViz.convert(graph));
        writer.write(toUppaal.convert(graph));
        writer.close();

        writer = new BufferedWriter(new FileWriter("constraint.conf"));
        boolean first = true;
        for(Constraint cnst : p.getConstraints()){
            writer.write(cnst.runtimeConstraintList(first));
            writer.flush();
            first = false;
        }
        writer.close();

    }
}
