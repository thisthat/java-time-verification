package intermediateModelHelper.envirorment.temporalTypes;


import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.types.definition.Duration;
import intermediateModel.types.definition.Timestamp;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeParameterMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TemporalTypes {

    public static String user_load_dir = "user.dir";

    private static List<TimeMethod> rt_t;
    private static List<TimeMethod> rt_d;
    private static List<TimeParameterMethod> et_t;
    private static List<TimeParameterMethod> et_d;

    private static TemporalTypes instance = null;

    protected TemporalTypes() {
        rt_t = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_t.csv")).getMethods();
        rt_d = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_d.csv")).getMethods();
        et_t = new ParseCSVTimeParameterMethods(getClass().getClassLoader().getResourceAsStream("semantics/et_t.csv")).getMethods();
        et_d = new ParseCSVTimeParameterMethods(getClass().getClassLoader().getResourceAsStream("semantics/et_d.csv")).getMethods();
        loadUserDefined();
    }

    public static synchronized TemporalTypes getInstance() {
        if (instance == null) {
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

    public static List<TimeParameterMethod> getETT() {
        return et_t;
    }

    public static List<TimeParameterMethod> getETD() {
        return et_d;
    }

    public void loadUserDefined() {
        String dir = System.getProperty(user_load_dir);
        if (!dir.endsWith("/"))
            dir += "/";
        dir += "config/";
        loadUserDefined(dir);
    }

    public void loadUserDefinedPrefix(String name) {
        String dir = System.getProperty(user_load_dir);
        if (!dir.endsWith("/"))
            dir += "/";
        dir += "config/" + name;
        loadUserDefined(dir);
    }

    public void loadUserDefined(String dir) {
        System.out.println("Loading from: " + new File(dir).getAbsolutePath());
        rt_t.addAll(new ParseCSVMethods(dir + "_rt_t.csv").getMethods());
        rt_d.addAll(new ParseCSVMethods(dir + "_rt_d.csv").getMethods());
        et_t.addAll(new ParseCSVTimeParameterMethods(dir + "_et_t.csv").getMethods());
        et_d.addAll(new ParseCSVTimeParameterMethods(dir + "_et_d.csv").getMethods());
    }

    public void loadUserTypes_RTT(String file) {
        rt_t.addAll(new ParseCSVMethods(file).getMethods());
    }

    public void loadUserTypes_RTD(String file) {
        rt_d.addAll(new ParseCSVMethods(file).getMethods());
    }

    public void loadUserTypes_ETT(String file) {
        et_t.addAll(new ParseCSVTimeParameterMethods(file).getMethods());
    }

    public void loadUserTypes_ETD(String file) {
        et_d.addAll(new ParseCSVTimeParameterMethods(file).getMethods());
    }

    private boolean isTimeMethod(ASTMethodCall m, List<? extends TimeMethod> l) {
        for (TimeMethod tm : l) {
            if (tm.isMethodCall(m))
                return true;
        }
        return false;
    }

    public boolean isRT_T(ASTMethodCall m) {
        return isTimeMethod(m, rt_t);
    }

    public boolean isRT_D(ASTMethodCall m) {
        return isTimeMethod(m, rt_d);
    }

    public boolean isET_T(ASTMethodCall m) {
        return isTimeMethod(m, et_t);
    }

    public boolean isET_D(ASTMethodCall m) {
        return isTimeMethod(m, et_d);
    }

    public int[] getTimeoutParametersET_D(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et_d) {
            if (tmp.isMethodCall(m)) {
                return tmp.getTimeouts();
            }
        }
        return new int[0];
    }

    public int[] getTimeoutParametersET_T(ASTMethodCall m) {
        TimeParameterMethod t = null;
        for (TimeParameterMethod tmp : et_t) {
            if (tmp.isMethodCall(m)) {
                t = tmp;
            }
        }
        if (t == null) return new int[0];
        return t.getTimeouts();
    }

    public TimeParameterMethod DEBUG_ETD(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et_d) {
            if (tmp.isMethodCall(m)) {
                return tmp;
            }
        }
        return null;
    }

    public TimeParameterMethod DEBUG_ETT(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et_t) {
            if (tmp.isMethodCall(m)) {
                return tmp;
            }
        }
        return null;
    }

    public void addRT_T(List<TimeMethod> rt) {
        rt_t.addAll(rt);
    }

    public void addRT_D(List<TimeMethod> rt) {
        rt_d.addAll(rt);
    }

    public void addET_T(List<TimeParameterMethod> et) {
        et_t.addAll(et);
    }

    public void addET_D(List<TimeParameterMethod> et) {
        et_d.addAll(et);
    }

    public void addRT(List<TimeTypes> rt) {
        for (TimeTypes t : rt) {
            if (t.getTimeType() instanceof Timestamp) {
                rt_t.add(new TimeMethod(t.getClassName(), t.getMethodName(), t.getSignature()));
            } else if (t.getTimeType() instanceof Duration) {
                rt_d.add(new TimeMethod(t.getClassName(), t.getMethodName(), t.getSignature()));
            }
        }
    }

    public void addET(List<intermediateModelHelper.envirorment.temporal.structure.TimeMethod> et) {
        for (intermediateModelHelper.envirorment.temporal.structure.TimeMethod t : et) {
            if (t.getTimeType() instanceof Timestamp) {
                et_t.add(new TimeParameterMethod(t.getClassName(), t.getMethodName(), t.getSignature(), t.getTimeouts()));
            } else if (t.getTimeType() instanceof Duration) {
                et_d.add(new TimeParameterMethod(t.getClassName(), t.getMethodName(), t.getSignature(), t.getTimeouts()));
            }
        }
    }

    public void addET(intermediateModelHelper.envirorment.temporal.structure.TimeMethod t) {
        if (t.getTimeType() instanceof Timestamp) {
            et_t.add(new TimeParameterMethod(t.getClassName(), t.getMethodName(), t.getSignature(), t.getTimeouts()));
        } else if (t.getTimeType() instanceof Duration) {
            et_d.add(new TimeParameterMethod(t.getClassName(), t.getMethodName(), t.getSignature(), t.getTimeouts()));
        }

    }
}
