package converter;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import slicing.Slice;
import slicing.model.Method;
import smt.TranslateReducedModel;
import smt.exception.VariableNotCorrect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class ClassAnalyzer {

    private final TranslateReducedModel translateReducedModel;
    HashMap<IASTMethod, Method> reducedModel = new HashMap<>();

    public ClassAnalyzer(ASTClass c) {
        reducedModel = Slice.slice(c);
        translateReducedModel = new TranslateReducedModel();
    }

    public List<VariableNotCorrect> getErrors(IASTMethod m){
        Method mm = reducedModel.get(m);
        if(mm == null)
            return new ArrayList<>();
        return getErrors(mm);
    }

    public List<VariableNotCorrect> getErrors(Method m){
        return translateReducedModel.check(m);
    }

    public HashMap<IASTMethod, List<VariableNotCorrect>> getErrors(){
        HashMap<IASTMethod, List<VariableNotCorrect>> out = new HashMap<>();
        for(IASTMethod m : reducedModel.keySet()){
            out.put(m, getErrors(m));
        }
        return out;
    }
}
