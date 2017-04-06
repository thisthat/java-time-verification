package intermediateModelHelper.envirorment.temporal;

import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporal.structure.TimeTimeout;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.envirorment.temporal.structure.TimeUndefinedTimeout;

import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TemporalInfo {

    private static List<TimeMethod>  timeMethods;
    private static List<TimeTimeout> timeTimeout;
    private static List<TimeTimeout> timeSoTimeout;
    private static List<TimeTimeout> readTimeout;
    private static List<TimeTypes>   timeTypes;
    private static List<TimeUndefinedTimeout>  timeUndefinedTimeout;

    private static TemporalInfo instance = null;

    protected TemporalInfo() {
        timeMethods = new ParseMethods( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/methods.csv")).getMethods();
        timeTimeout = new ParseTimeout( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/timeout.csv")).getMethods();
        readTimeout = new ParseTimeout( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/readtimeout.csv")).getMethods();
        timeTypes   = new ParseTypes(   getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/types.csv")).getMethods();
        timeUndefinedTimeout   = new ParseUndefinedTimeout(   getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/undefinedTimeout.csv")).getMethods();
        loadUserDefined();
    }

    private void loadUserDefined() {
        String dir = System.getProperty("user.dir");
        if(!dir.endsWith("/"))
            dir += "/";
        dir += "config/";
        timeMethods.addAll( new ParseMethods(dir + "methods.csv").getMethods());
        timeTimeout.addAll( new ParseTimeout(dir + "timeout.csv").getMethods());
        readTimeout.addAll( new ParseTimeout(dir + "readtimeout.csv").getMethods());
        timeTypes.addAll(   new ParseTypes  (dir + "types.csv").getMethods());
        timeUndefinedTimeout.addAll(   new ParseUndefinedTimeout  (dir + "undefinedTimeout.csv").getMethods());
    }


    public static synchronized TemporalInfo getInstance(){
        if(instance == null) {
            instance = new TemporalInfo();
        }
        return instance;
    }

    public void addTimeMethods(List<TimeMethod> timeMethods) {
        TemporalInfo.timeMethods.addAll(timeMethods);
    }

    public void addTimeTimeout(List<TimeTimeout> timeTimeout) {
        TemporalInfo.timeTimeout.addAll(timeTimeout);
    }

    public void addTimeSoTimeout(List<TimeTimeout> timeSoTimeout) {
        TemporalInfo.timeSoTimeout.addAll(timeSoTimeout);
    }

    public void addReadTimeout(List<TimeTimeout> readTimeout) {
        TemporalInfo.readTimeout.addAll(readTimeout);
    }

    public static void addTimeUndefinedTimeout(List<TimeUndefinedTimeout> timeUndefinedTimeout) {
        TemporalInfo.timeUndefinedTimeout.addAll(timeUndefinedTimeout);
    }

    public void addTimeTypes(List<TimeTypes> timeTypes) {
        TemporalInfo.timeTypes.addAll(timeTypes);
    }

    public List<TimeMethod> getTimeMethods() {
        return timeMethods;
    }

    public List<TimeTimeout> getTimeTimeout() {
        return timeTimeout;
    }

    public List<TimeTimeout> getTimeSoTimeout() {
        return timeSoTimeout;
    }

    public List<TimeTimeout> getReadTimeout() {
        return readTimeout;
    }

    public List<TimeTypes> getTimeTypes() {
        return timeTypes;
    }

    public List<TimeUndefinedTimeout> getTimeUndefinedTimeout() {
        return timeUndefinedTimeout;
    }
}
