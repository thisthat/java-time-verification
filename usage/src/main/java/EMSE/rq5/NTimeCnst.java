package EMSE.rq5;

import PCFG.creation.IM2CFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.metrics.CountVars;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giovanni on 02/05/2017.
 */

public class NTimeCnst {

    static String connectionQuery = "jdbc:postgresql://brock.isys.uni-klu.ac.at:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO public.\"NumberTimeInfoPerMethod\"" +
            "(\"fileID\", \"nTimeInfo\", \"nMethods\")" +
            " VALUES (?, ?, ?);";


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

        new NTimeCnst().doWork(base_path, fileName, className, methodName, signature, id);
    }

    public void doWork(String base_path, String filename, String className, String methodName, String signature, String id) throws Exception {
        List<ASTClass> cs = JDTVisitor.parse(filename, base_path, ElseIf.filter);
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

        IM2CFG p = new IM2CFG();
        long cnst = p.addClassAndGetSize(_zlass, _method);

        saveDB(
                id,
                cnst,
                _zlass.getCountMethod()
        );
        //closeFiles();
    }

    private void saveDB(String id, long nCnst, long nMethods) throws Exception {
        Connection conn = DriverManager.getConnection(connectionQuery);
        PreparedStatement st = conn.prepareStatement(sqlInsert);
        st.setLong(1, Long.parseLong(id));
        st.setLong(2, nCnst);
        st.setLong(3, nMethods);
        st.execute();
    }
}
