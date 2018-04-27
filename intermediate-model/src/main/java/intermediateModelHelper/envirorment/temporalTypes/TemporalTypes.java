package intermediateModelHelper.envirorment.temporalTypes;


import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeMethod;

import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TemporalTypes {

    public static String user_load_dir = "user.dir";

    private static List<TimeMethod> rt_t;
    private static List<TimeMethod> rt_d;
    private static List<TimeMethod> et_t;
    private static List<TimeMethod> et_d;

    private static TemporalTypes instance = null;

    protected TemporalTypes() {
        rt_t   = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_t.csv")).getMethods();
        rt_d   = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_d.csv")).getMethods();
        et_t   = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/et_t.csv")).getMethods();
        et_d   = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/et_d.csv")).getMethods();
        loadUserDefined();
    }

    public void loadUserDefined() {
        String dir = System.getProperty(user_load_dir);
        if(!dir.endsWith("/"))
            dir += "/";
        dir += "config/";
        loadUserDefined(dir);
    }

    public void loadUserDefined(String dir) {
        rt_t.addAll( new ParseCSVMethods(dir + "rt_t.csv").getMethods());
        rt_d.addAll( new ParseCSVMethods(dir + "rt_d.csv").getMethods());
        et_t.addAll( new ParseCSVMethods(dir + "et_t.csv").getMethods());
        et_d.addAll( new ParseCSVMethods(dir + "et_d.csv").getMethods());
    }

    public void loadUserTypes_RTT(String file){
        rt_t.addAll(new ParseCSVMethods(file).getMethods());
    }
    public void loadUserTypes_RTD(String file){
        rt_d.addAll(new ParseCSVMethods(file).getMethods());
    }
    public void loadUserTypes_ETT(String file){
        et_t.addAll(new ParseCSVMethods(file).getMethods());
    }
    public void loadUserTypes_ETD(String file){
        et_d.addAll(new ParseCSVMethods(file).getMethods());
    }


    private boolean isTimeMethod(ASTMethodCall m, List<TimeMethod> l){
        for(TimeMethod tm : l){
            if(tm.isMethodCall(m))
                return true;
        }
        return false;
    }

    public boolean isRT_T(ASTMethodCall m){
        return isTimeMethod(m, rt_t);
    }
    public boolean isRT_D(ASTMethodCall m){
        return isTimeMethod(m, rt_d);
    }
    public boolean isET_T(ASTMethodCall m){
        return isTimeMethod(m, et_t);
    }
    public boolean isET_D(ASTMethodCall m){
        return isTimeMethod(m, et_d);
    }




    public static synchronized TemporalTypes getInstance(){
        if(instance == null) {
            instance = new TemporalTypes();
        }
        return instance;
    }

    public static List<TimeMethod> getRTT() {
        return rt_t;
    }

    public static List<TimeMethod> getRTD() {
        return rt_d;
    }

    public static List<TimeMethod> getETT() {
        return et_t;
    }

    public static List<TimeMethod> getETD() {
        return et_d;
    }
}
