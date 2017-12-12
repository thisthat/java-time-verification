package daikon.annotation;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.envirorment.Env;

import java.util.*;

public class TimeVarCollector extends ParseIM {

    WatchingPoints wp;
    public WatchingPoints getTimeVariables(ASTClass c){
        wp = new WatchingPoints();
        super.start(c);
        return wp;
    }

    private void analyze(IASTStm elm, Env env, String className, String methodName){
        analyze(elm.getLineEnd(), env, className, methodName);
    }
    private void analyze(int line, Env env, String className, String methodName){
        Set<IASTVar> tVars = computeVars(env);
        if(tVars.size() > 0){
            wp.addWatchingPoint(className, methodName, line, tVars);
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
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTTryResources(ASTTryResources elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTFinally(ASTTry.ASTFinallyBranch elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTTry(ASTTry elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTCatch(ASTTry.ASTCatchBranch elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTSynchronized(ASTSynchronized elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeElseBranch(ASTIf.ASTElseStms elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeIfBranch(ASTIf.ASTIfStms elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTForEach(ASTForEach elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTFor(ASTFor elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTDoWhile(ASTDoWhile elm, Env env) {
        analyze(elm, env, super.getLastClass(), super.getLastMethod());
    }

    @Override
    protected void postAnalyzeASTMethod(IASTMethod elm, Env env) {
        analyze(elm.getLineEnd(), env, super.getLastClass(), super.getLastMethod());
    }


}
