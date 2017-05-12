import PCFG.converter.IConverter;
import PCFG.converter.ToUppaal;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.structure.RuntimeConstraint;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by giovanni on 02/05/2017.
 */

/**
 +++++++++++ issue KAFKA-3540 --
 - ecff8544dd45e8cf0fcf04f5e0e716d3e21c9f20
 = f72203ee9223d3b724ee67bdad9912612dd72f63
/Users/giovanni/repository/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/ConsumerCoordinator.java
ConsumerCoordinator
commitOffsetsSync
kafka_3540.xml
constraint_kafka_3540.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue KAFKA-4426 --
 - ecff8544dd45e8cf0fcf04f5e0e716d3e21c9f20
 = f72203ee9223d3b724ee67bdad9912612dd72f63
 /Users/giovanni/repository/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/AbstractCoordinator.java
 AbstractCoordinator
 ensureCoordinatorReady
 kafka_4426.xml
 constraint_kafka_4426.conf
 /Users/giovanni/repository/kafka/clients/src/main/java

 */
public class t {

    public static void main(String[] args) throws Exception {
        String f = t.class.getClassLoader().getResource("exampleTraces/UndefinedTimeBehaviour.java").getFile();
        List<ASTClass> cs = JDTVisitor.parse( f, f.substring(0, f.lastIndexOf("/")));
        ExtractModelAndCnst.def(cs.get(0),cs.get(0).getAllMethods().get(0), "model.xml", "ff.txt");
    }

}
