package intermediateModelHelper.envirorment.temporalTypes.structure;

import intermediateModel.structure.expression.ASTMethodCall;

import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TimeParameterMethod extends TimeMethod {

    private int[] timeouts;

    public TimeParameterMethod(String className, String methodName, List<String> signature, int[] timeouts) {
        super(className, methodName, signature);
        this.timeouts = timeouts;
    }

    public int[] getTimeouts() {
        return timeouts;
    }


    @Override
    public String toString() {
        StringBuilder sign = new StringBuilder();
        List<String> signature = getSignature();
        int last = signature.size() - 1;
        for(int i = 0; i <= last; i++){
            if(i == last){
                sign.append(signature.get(i));
            } else {
                sign.append(signature.get(i)).append(",");
            }
        }
        return String.format("%s;%s;%s;long", getClassName(), getMethodName(), sign.toString());
    }

    public boolean isMethodCall(ASTMethodCall m) {
        if(m.getClassPointed() == null) return false;
        if(m.getMethodName() == null) return false;
        if(!m.getClassPointed().equals(getClassName())) return false;
        if(!m.getMethodName().equals(getMethodName())) return false;
        //if(m.getParameters().size() != signature.size()) return false;
        return true;
    }
}
