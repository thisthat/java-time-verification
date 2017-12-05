package intermediateModelHelper.envirorment.temporal.structure;

import intermediateModel.structure.expression.ASTMethodCall;

import java.util.List;

/**
 * Created by giovanni on 07/03/2017.
 */
public class TimeTypes extends TimeInfo {

    public TimeTypes(String className, String methodName, List<String> signature) {
        super(className, methodName, signature);
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
        return String.format("%s;%s;%s;long", className, methodName, sign.toString());
    }

    public boolean isMethodCall(ASTMethodCall m) {
        if(m.getClassPointed() == null) return false;
        if(m.getMethodName() == null) return false;
        if(!m.getClassPointed().equals(className)) return false;
        if(!m.getMethodName().equals(methodName)) return false;
        //if(m.getParameters().size() != signature.size()) return false;
        return true;
    }
}
