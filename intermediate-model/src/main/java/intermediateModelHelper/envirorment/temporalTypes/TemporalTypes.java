package intermediateModelHelper.envirorment.temporalTypes;


import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.types.definition.Duration;
import intermediateModel.types.definition.TimeType;
import intermediateModel.types.definition.Timestamp;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
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
    private static List<TimeParameterMethod> et;

    private static TemporalTypes instance = null;

    protected TemporalTypes() {
        rt_t = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_t.csv")).getMethods();
        rt_d = new ParseCSVMethods(getClass().getClassLoader().getResourceAsStream("semantics/rt_d.csv")).getMethods();
        et = new ParseCSVTimeParameterMethods(getClass().getClassLoader().getResourceAsStream("semantics/et.csv")).getMethods();
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

    public static List<TimeParameterMethod> getET() {
        return et;
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
        dir += "config/" + name + "_";
        loadUserDefined(dir);
    }

    public void loadUserDefined(String dir) {
        TemporalInfo.getInstance().loadUserDefined(dir);
        System.out.println("Loading from: " + new File(dir).getAbsolutePath());
        rt_t.addAll(new ParseCSVMethods(dir + "rt_t.csv").getMethods());
        rt_d.addAll(new ParseCSVMethods(dir + "rt_d.csv").getMethods());
        et.addAll(new ParseCSVTimeParameterMethods(dir + "et.csv").getMethods());
    }

    public void loadUserTypes_RTT(String file) {
        rt_t.addAll(new ParseCSVMethods(file).getMethods());
    }

    public void loadUserTypes_RTD(String file) {
        rt_d.addAll(new ParseCSVMethods(file).getMethods());
    }

    public void loadUserTypes_ET(String file) {
        et.addAll(new ParseCSVTimeParameterMethods(file).getMethods());
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

    public boolean isET(ASTMethodCall m) {
        return isTimeMethod(m, et);
    }


    public int[] getTimeoutParametersET(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et) {
            if (tmp.isMethodCall(m)) {
                return tmp.getTimeouts();
            }
        }
        return new int[0];
    }
    public TimeType[] getTypeParametersET(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et) {
            if (tmp.isMethodCall(m)) {
                return tmp.getTimeType();
            }
        }
        return new TimeType[0];
    }
    public List<String> getSignatureET(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et) {
            if (tmp.isMethodCall(m)) {
                return tmp.getSignature();
            }
        }
        return new ArrayList<>();
    }


    public TimeParameterMethod DEBUG_ET(ASTMethodCall m) {
        for (TimeParameterMethod tmp : et) {
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

    public void addET(List<TimeParameterMethod> et) {
        this.et.addAll(et);
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
}
