package EMSE.rq5;

import PCFG.converter.IConverter;
import PCFG.converter.ToUppaal;
import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import helper.ResourceInfo;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.metrics.CyclomaticComplexity;
import intermediateModel.metrics.NumberOfStatements;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giovanni on 02/05/2017.
 */

public class SpaceTimeFootprint {

    static BufferedWriter timeWriter;
    static BufferedWriter memoryWriter;

    static String connectionQuery = "jdbc:postgresql://brock.isys.uni-klu.ac.at:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO public.\"RQ5_result\"" +
            "(\"fileID\", \"parsingTime\", \"computingTime\", \"totTime\", \"memoryUsage\", \"cyclomaticComplexity\", \"numberOfStatements\")" +
            " VALUES (?, ?, ?, ?, ?, ?, ?);";




    char s;

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println("Expected project 6 arguments, given " + args.length);
            Arrays.stream(args).forEach(System.out::println);
            return;
        }
        //debug info
        System.out.println("Processing: ");
        Arrays.stream(args).forEach(System.out::println);
        System.out.println("===========================================");

        String base_path = args[0];
        String fileName = args[1];
        String className = args[2];
        String methodName = args[3];
        String signature = args[4];
        String id = args[5];

        String name = base_path.substring(base_path.lastIndexOf("/")+1);
        String dir = System.getProperty("user.dir");
        if(!dir.endsWith("/"))
            dir += "/";
        dir += "config/" + name + "_";
        TemporalInfo.getInstance().loadUserDefined(dir);

        new SpaceTimeFootprint().doWork(base_path, fileName, className, methodName, signature, id);
    }

    public void doWork(String base_path, String filename, String className, String methodName, String signature, String id) throws Exception {

        long startTimeParsing, endTimeParsing, totParsing;
        long startTime, endTime;

        long startMemory, endMemory;


        startTimeParsing = System.currentTimeMillis();
        List<ASTClass> cs = JDTVisitor.parse(filename, base_path, ElseIf.filter);
        endTimeParsing = System.currentTimeMillis();
        //totParsing = endTimeParsing - startTimeParsing;

        ASTClass _zlass = null;
        IASTMethod _method = null;

        for(ASTClass c : cs){
            if(c.fullName().equals(className)){
                _zlass = c;
                break;
            }
        }
        assert _zlass != null;

        for(IASTMethod m : _zlass.getMethods()){
            String sign = Arrays.toString(m.getSignature().toArray(new String[0]));
            if(m.getName().equals(methodName) && sign.equals(signature)){
                _method = m;
                break;
            }
        }
        assert _method != null;

        forceGC();

        //Time/Memory utilization
        startMemory = getMemory();
        startTime = System.currentTimeMillis();

        IM2CFG p = new IM2CFG();
        p.addClass(_zlass, _method);
        PCFG graph = p.buildPCFG();
        graph.optimize();
        graph.optimize(new OptimizeTimeAutomata());
        IConverter toUppaal = new ToUppaal(_zlass, ToUppaal.NAMING.LINE);
        String model = toUppaal.convert(graph);
        //dummy operation to force model to be stored
        s = model.charAt(0);

        //Time/Memory utilization
        endTime = System.currentTimeMillis();
        endMemory = getMemory();
        //computeTime(startTime, endTime, totParsing, id);
        //computeMemory(startMemory, endMemory, id);

        saveDB(
                id,
                endTimeParsing - startTimeParsing,
                endTime - startTime,
                endMemory - startMemory,
                CyclomaticComplexity.get(_method),
                NumberOfStatements.get(_method)
        );
        //closeFiles();
    }

    private void saveDB(String id, long parsingTime, long computeTime, long memoryUsage, long cyclomaticComplexity, long numberOfStatements) throws Exception {
        Connection conn = DriverManager.getConnection(connectionQuery);
        PreparedStatement st = conn.prepareStatement(sqlInsert);
        st.setLong(1, Long.parseLong(id));
        st.setLong(2, parsingTime);
        st.setLong(3, computeTime);
        st.setLong(4, parsingTime + computeTime);
        st.setLong(5, memoryUsage);
        st.setLong(6, cyclomaticComplexity);
        st.setLong(7, numberOfStatements);
        st.execute();
    }

    private void closeFiles() throws IOException {
        memoryWriter.close();
        timeWriter.close();
    }

    private static void computeTime(long startTime, long endTime, long totParsing, String id) {
        long point = endTime - startTime;
        try {
            timeWriter.write(String.format("%s;%d;%d;%d\n", id, totParsing, point, totParsing+point));
            timeWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void computeMemory(long startMemory, long endMemory, String id) {
        long point = endMemory - startMemory;
        try {
            memoryWriter.write(String.format("%s;%d\n", id, point));
            memoryWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long getMemory(){
        return getCurrentlyUsedMemory();
    }

    long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) { sum +=  count; }
        }
        return sum;
    }

    void forceGC(){
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before);
    }
    long getReallyUsedMemory() {
        forceGC();
        return getCurrentlyUsedMemory();
    }

    long getCurrentlyUsedMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    long getSettledUsedMemory() {
        long m;
        long m2 = getReallyUsedMemory();
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //ignore
            }
            m = m2;
            m2 = getCurrentlyUsedMemory();
        } while (m2 < getReallyUsedMemory());
        return m;
    }
}
