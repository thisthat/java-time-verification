package automata;

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

 +++++++++++ issue KAFKA-4290 --
 - 72d5675a7cfc64a5547b921672856ea4afc4f4c2
 = 8c367790fa1dd337a8a4bad393cd856dfcd92c31
/Users/giovanni/repository/kafka/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/WorkerCoordinator.java
WorkerCoordinator
poll
kafka_4290.xml
constraint_kafka_4290.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue KAFKA-4306 --
 - 72d5675a7cfc64a5547b921672856ea4afc4f4c2
 = c9e99f297f3090cd348e231dbeaf69c388de1234
/Users/giovanni/repository/kafka/connect/runtime/src/main/java/org/apache/kafka/connect/runtime/distributed/DistributedHerder.java
DistributedHerder
stop
kafka_4306.xml
constraint_kafka_4306.conf
/Users/giovanni/repository/kafka/clients/src/main/java

 +++++++++++ issue LENS-1032 --
 - 3828819403c7c27c25e1d7d2421d0dcc5f506cdf
 = 389daac9a66b0cb69ac729a56b39b3aa903ad36a
/Users/giovanni/repository/lens/lens-server/src/main/java/org/apache/lens/server/query/QueryExecutionServiceImpl.java
QueryExecutionServiceImpl
executeTimeoutInternal
lens_1032.xml
constraint_lens_1032.conf
/Users/giovanni/repository/lens/lens-server/src/main/java


 +++++++++++ issue FLUME-3044 --
 - 73d87444013a656f763feb38ce20c43670dc6230
 =
/Users/giovanni/repository/flume/flume-ng-sinks/flume-ng-kafka-sink/src/main/java/org/apache/flume/sink/kafka/KafkaSink.java
KafkaSink
process
flume_3044.xml
constraint_flume_3044.conf
/Users/giovanni/repository/flume/flume-ng-sinks/flume-ng-kafka-sink/src/main/java



 +++++++++++ issue HBASA-17341 --
 - e1f4aaeacdcbaffb02a08c29493601547c381941
 = cac0904c16dde9eb7bdbb57e4a33224dd4edb77f
/Users/giovanni/repository/hbase/hbase-server/src/main/java/org/apache/hadoop/hbase/replication/regionserver/ReplicationSource.java
ReplicationSource
terminate
hbase_17341.xml
constraint_hbase_17341.conf
/Users/giovanni/repository/hbase/hbase-server/src/main/java
 */
public class ExtractModelAndCnst {

    public static void main(String[] args) throws Exception {
        if(args.length != 6){
            System.err.println("Usage: filePath className methodName modelName constraintName envPath");
            return;
        }

        String file = args[0];
        String className = args[1];
        String methodName = args[2];
        String modelName = args[3];
        String outputName = args[4];
        List<ASTClass> cs = JDTVisitor.parse( file, args[5]);

        System.out.println(">> Writing model: " + modelName);
        System.out.println(">> Writing conf file: " + outputName);
        ASTClass c = null;
        for(ASTClass cc : cs){
            if(cc.getName().equals(className))
                c = cc;
        }
        if(c == null) {
            System.err.println("Cannot found the correct class");
            System.exit(1);
        }
        IASTMethod m = c.getFirstMethodByName(methodName);
        def(c,m, modelName, outputName);
    }

    public static void def(ASTClass c, IASTMethod m, String nameModel, String nameConstraint) throws Exception {

        IM2CFG p = new IM2CFG();
        p.addClass(c,m);
        PCFG graph = p.buildPCFG();
        graph.optimize();
        graph.optimize(new OptimizeTimeAutomata());

        BufferedWriter writer = null;
        //writer = new BufferedWriter(new FileWriter("graph.xal"));
        writer = new BufferedWriter(new FileWriter(nameModel));
        IConverter toUppaal = new ToUppaal(c, ToUppaal.NAMING.LINE);
        //writer.write(toGraphViz.convert(graph));
        writer.write(toUppaal.convert(graph));
        writer.close();

        writer = new BufferedWriter(new FileWriter(nameConstraint));
        boolean first = true;
        for(RuntimeConstraint cnst : p.getResetRuntimeConstraints()){
            if(first){
                writer.write("class;method;line;var\n");
            }
            writer.write(cnst.toString() + "\n");
            writer.flush();
            first = false;
        }
        writer.close();
    }
}
