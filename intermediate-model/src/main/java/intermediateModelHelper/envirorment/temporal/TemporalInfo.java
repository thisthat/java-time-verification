package intermediateModelHelper.envirorment.temporal;

import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporal.structure.TimeTimeout;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;

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

    private static TemporalInfo instance = null;

    protected TemporalInfo() {
        timeMethods = new ParseMethods( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/methods.csv")).getMethods();
        timeTimeout = new ParseTimeout( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/timeout.csv")).getMethods();
        readTimeout = new ParseTimeout( getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/readtimeout.csv")).getMethods();
        timeTypes   = new ParseTypes(   getClass().getClassLoader().getResourceAsStream("descriptorTimeRelevant/types.csv")).getMethods();
    }

    public static synchronized TemporalInfo getInstance(){
        if(instance == null) {
            instance = new TemporalInfo();
        }
        return instance;
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
}
