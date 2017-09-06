package intermediateModel.visitors;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTAttribute;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTHiddenClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTAssignment;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.EnvBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 13/03/2017.
 */
public class ExtractTimeAttribute extends ParseIM {

    List<IASTVar> timeAttributes = new ArrayList<>();

    public ExtractTimeAttribute(ASTClass _class) {
        super(_class);
        this.start(_class);
    }

    @Override
    public void start(ASTClass c) {
        timeAttributes.clear();
        int size;
        do {
            size = timeAttributes.size();
            Env finalEnv = this.createBaseEnv(c);
            for (IASTVar v : finalEnv.getVarList()) {
                if (!v.isTimeCritical()) {
                    continue;
                }
                if (!timeAttributes.contains(v))
                    timeAttributes.add(v);
            }
        } while(size != timeAttributes.size());
    }

    public List<IASTVar> getTimeAttributes() {
        return timeAttributes;
    }

    @Override
    protected EnvBase createBaseEnv(ASTClass c){
        super.createBaseEnv(c);
        for(IASTVar v : base_env.getVarList()){
            if(timeAttributes.contains(v)){
                v.setTimeCritical(true);
            }
        }
        //check method
        for (IASTMethod m : c.getMethods()) {
            Env eMethod = new Env(base_env);
            eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
            super.analyze(m.getStms(), eMethod);
        }
        //check attributes
        for(ASTAttribute a : c.getAttributes()){
            analyzeAttribute(a, base_env);
        }
        return base_env;
    }

    private void analyzeAttribute(ASTAttribute a, EnvBase base_env) {
        if(a.isTimeCritical() && a.getExpr() != null){
            IASTRE expr = a.getExpr().getExpression();
            int s = a.getStart();
            int e = a.getEnd();
            ASTAssignment ass = new ASTAssignment(s, e, new ASTLiteral(s, e, a.getName()), expr, IASTRE.OPERATOR.equal);
            ASTRE re = new ASTRE(s, e, ass);
            CheckExpression.checkRE(re, base_env);
        }
    }

    @Override
    protected void endAnalyzeHiddenClass(ASTHiddenClass elm, Env env) {
        int size;
        do {
            size = timeAttributes.size();
            for (IASTMethod m : elm.getMethods()) {
                Env eMethod = new Env(env);
                eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
                super.analyze(m.getStms(), eMethod);
            }
            for (IASTVar v : env.getAllVarList()) {
                if (!v.isTimeCritical()) {
                    continue;
                }
                if (!timeAttributes.contains(v))
                    timeAttributes.add(v);
            }
        } while(size != timeAttributes.size());
    }

    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        CheckExpression.checkRE(r, env);
        CheckExpression.checkMethodCall(r, env);
    }
}
