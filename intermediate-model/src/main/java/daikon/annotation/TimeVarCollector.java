package daikon.annotation;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.envirorment.Env;

import java.util.*;

public class TimeVarCollector extends ParseIM {

    Map<Integer, Set<IASTVar>> variables = new LinkedHashMap<>();

    public Map<Integer, Set<IASTVar>> getTimeVariables(ASTClass c){
        variables.clear();
        super.start(c);
        return variables;
    }

    private void analyze(IASTStm elm, Env env){
        analyze(elm.getLineEnd(), env);
    }
    private void analyze(int line, Env env){
        Set<IASTVar> tVars = computeVars(env);
        if(tVars.size() > 0){
            variables.put(line, tVars);
        }
    }

    private Set<IASTVar> computeVars(Env env) {
        Set<IASTVar> out = new HashSet<>();
        for(IASTVar v : env.getAllVarList()){
            if(v.isTimeCritical())
                out.add(v);
        }
        return out;
    }

    @Override
    protected void postAnalyzeASTWhile(ASTWhile elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTTryResources(ASTTryResources elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTFinally(ASTTry.ASTFinallyBranch elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTTry(ASTTry elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTCatch(ASTTry.ASTCatchBranch elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTSynchronized(ASTSynchronized elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeElseBranch(ASTIf.ASTElseStms elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeIfBranch(ASTIf.ASTIfStms elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTForEach(ASTForEach elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTFor(ASTFor elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTDoWhile(ASTDoWhile elm, Env env) {
        analyze(elm, env);
    }

    @Override
    protected void postAnalyzeASTMethod(IASTMethod elm, Env env) {
        analyze(elm.getLineEnd(), env);
    }


}
