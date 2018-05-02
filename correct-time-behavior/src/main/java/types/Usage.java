package types;

import debugger.Debugger;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTVar;
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
        Debugger.getInstance(false).log("Line " + r.getLine() + " : " + t);
        System.out.println("_________");
        for(IASTVar v : env.getAllVarList()){
            System.out.println(v.getName() + " -> " + v.getVarTimeType());
        }
        System.out.println("_________");
    }

    @Override
    protected void postAnalyzeASTMethod(IASTMethod elm, Env env) {

    }
}
