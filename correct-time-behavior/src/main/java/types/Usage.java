package types;

import intermediateModel.structure.ASTRE;
import intermediateModel.typedefinition.TimeType;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.envirorment.Env;
import types.rules.TypeResolver;

public class Usage extends ParseIM  {



    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        if(r.getExpression() == null){
            return;
        }
        TimeType t =TypeResolver.resolveTimerType(r.getExpression(), env);

    }
}
