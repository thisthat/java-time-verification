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
    private static List<TimeTypes>   timeTypes;

    private static TemporalInfo instance = null;

    protected TemporalInfo() {
        timeMethods = new ParseMethods( getClass().getClassLoader().getResource("descriptorTimeRelevant/methods.csv").getFile()).getMethods();
        timeTimeout = new ParseTimeout( getClass().getClassLoader().getResource("descriptorTimeRelevant/timeout.csv").getFile()).getMethods();
        timeTypes   = new ParseTypes(   getClass().getClassLoader().getResource("descriptorTimeRelevant/types.csv")  .getFile()).getMethods();
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

    public List<TimeTypes> getTimeTypes() {
        return timeTypes;
    }
}
