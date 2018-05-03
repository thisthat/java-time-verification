package intermediateModelHelper.envirorment.temporal.structure;

import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.types.definition.TimeType;
import intermediateModel.types.definition.Unknown;

import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TimeMethod extends TimeInfo {

    private int[] timeouts;
    TimeType timeType;

    public TimeMethod(String className, String methodName, List<String> signature, int[] timeouts) {
        super(className, methodName, signature);
        this.timeouts = timeouts;
        timeType = new Unknown();
    }

    public TimeMethod(String className, String methodName, List<String> signature, int[] timeouts, TimeType timeType) {
        super(className, methodName, signature);
        this.timeouts = timeouts;
        this.timeType = timeType;
    }

    public TimeType getTimeType() {
        return timeType;
    }

    public int[] getTimeouts() {
        return timeouts;
    }

    @Override
    public String toString() {
        StringBuilder sign = new StringBuilder();
        int last = signature.size() - 1;
        for(int i = 0; i <= last; i++){
            if(i == last){
                sign.append(signature.get(i));
            } else {
                sign.append(signature.get(i)).append(",");
            }
        }
        StringBuilder ti = new StringBuilder();
        for(int i = 0; i < timeouts.length; i++){
            if(i == timeouts.length -1)
                ti.append(timeouts[i]);
            else
                ti.append(timeouts[i]).append(",");
        }
        return String.format("%s;%s;%s;%s", className, methodName, sign.toString(), ti.toString());
    }

    public boolean isMethodCall(ASTMethodCall m) {
        if(m.getClassPointed() == null) return false;
        if(m.getMethodName() == null) return false;
        if(!m.getClassPointed().equals(className)) return false;
        if(!m.getMethodName().equals(methodName)) return false;
        //if(m.getParameters().size() != signature.size()) return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeTypes)) return false;
        TimeTypes oo = (TimeTypes) o;
        if(!className.equals(oo.className)) return false;
        if(!methodName.equals(oo.methodName)) return false;
        if(!signature.equals(oo.signature)) return false;
        return timeType.equals(oo.timeType);
    }
}
