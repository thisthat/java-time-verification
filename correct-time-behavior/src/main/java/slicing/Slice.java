package slicing;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.EnvParameter;
import slicing.heuristics.*;
import slicing.model.If;
import slicing.model.Method;
import slicing.model.interfaces.Stm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by giovanni on 06/07/2017.
 */
public class Slice {

    static ApplyHeuristics ah = new ApplyHeuristics();
    static {
        ah.subscribe(MarkTime.class);
        ah.subscribe(MinMaxSearch.class);
        ah.subscribe(TimeInSignature.class);
        ah.subscribe(AssignmentTimeVar.class);
        ah.subscribe(BooleanExpression.class);
        ah.subscribe(ReturnExpression.class);
        ah.subscribe(PrintTimeVar.class);
    }

    private ASTClass _class;
    private HashMap<IASTMethod,Method> slices = new LinkedHashMap<>();
    private List<Stm> current;
    private List<IASTStm> timeStms;


    public static void slice(ASTClass c) {
        TimeStatements timeStms = TimeStatements.getInstance();
        timeStms.clear();
        ah.analyze(c);
        //debug
        Slice slicer = new Slice(c);
        slicer.start();
    }

    private Slice(ASTClass _class) {
        this._class = _class;
        timeStms = TimeStatements.getInstance().getStms();
    }

    private void start() {
        slices.clear();
        for(IASTMethod m : _class.getMethods()){
            Method mm = new Method(m.getStart(),m.getEnd(),m.getLine(),m.getLineEnd(),m.getCode());
            mm.setName(m.getName());
            mm.setSignature(m.getSignature());
            current = mm.getBody();
            slices.put(m, mm);
            analyze(m);
        }
    }

    private void analyze(IASTMethod m) {
        analyze(m.getStms());
    }

    private void analyze(List<IASTStm> stms) {
        for(IASTStm stm : stms) {
            if (stm instanceof ASTRE){
                analyze((ASTRE) stm);
            }
            else if(stm	instanceof ASTDoWhile){
                analyze((ASTDoWhile)stm);
            }
            else if(stm	instanceof ASTFor){
                analyze((ASTFor)stm);
            }
            else if(stm	instanceof ASTForEach){
                analyze((ASTForEach)stm);
            }
            else if(stm	instanceof ASTIf){
                analyze((ASTIf)stm);
            }
            else if(stm	instanceof ASTReturn){
                analyze((ASTReturn)stm);
            }
            else if(stm	instanceof ASTSwitch){
                analyze((ASTSwitch)stm);
            }
            else if(stm	instanceof ASTSynchronized){
                analyze((ASTSynchronized)stm);
            }
            else if(stm	instanceof ASTThrow){
                analyze((ASTThrow)stm);
            }
            //the order is important because ASTTry is extended by ASTTryResources
            else if(stm	instanceof ASTTryResources){
                analyze((ASTTryResources)stm);
            }
            else if(stm	instanceof ASTTry){
                analyze((ASTTry)stm);
            }
            else if(stm	instanceof ASTWhile){
                analyze((ASTWhile)stm);
            }
            else if(stm instanceof ASTNewObject){
                analyze((ASTNewObject) stm);
            }
            else if(stm instanceof ASTHiddenClass){
                analyze((ASTHiddenClass)stm);
            }
            else {
                analyze(stm);
            }
        }
    }

    private void analyze(ASTHiddenClass stm) {
        for(IASTMethod m : stm.getAllMethods()){
            analyze(m);
        }
    }

    private void analyze(ASTNewObject stm) {
        ASTHiddenClass hc = stm.getHiddenClass();
        if(hc != null){
            analyze(hc);
        }
        for(IASTRE p : stm.getParameters()){
            if(p instanceof ASTNewObject){
                analyze((ASTNewObject) p);
            }
        }
    }

    private void analyze(ASTWhile stm) {
        analyze(stm.getExpr());
        analyze(stm.getStms());
    }

    private void analyze(ASTDoWhile stm) {
        analyze(stm.getExpr());
        analyze(stm.getStms());
    }

    private void analyze(ASTTry stm) {
        analyze(stm.getTryBranch().getStms());
        for(ASTTry.ASTCatchBranch catchBranch : stm.getCatchBranch()){
            analyze( catchBranch.getStms() );
        }
        if(stm.getFinallyBranch() != null)
            analyze(stm.getFinallyBranch().getStms());
    }

    private void analyze(ASTTryResources stm) {
        for(ASTRE r : stm.getResources()){
            analyze(r);
        }
        analyze((ASTTry) stm);
    }

    private void analyze(ASTThrow stm) {
        analyze(stm.getExpr());
    }

    private void analyze(ASTSynchronized stm) {
        analyze(stm.getExpr());
        analyze(stm.getStms());
    }

    private void analyze(ASTSwitch stm) {
        analyze(stm.getExpr());
        for (ASTSwitch.ASTCase c : stm.getCases()) {
            analyze( c.getStms() );
        }
    }

    private void analyze(ASTReturn stm) {
        if(stm.getExpr() != null)
            analyze(stm.getExpr());
    }

    private void analyze(ASTIf stm) {
        analyze(stm.getGuard());
        analyze(stm.getIfBranch().getStms());
        if(stm.getElseBranch() != null) {
            analyze(stm.getElseBranch().getStms());
        }
    }

    private void analyze(ASTForEach stm) {
        analyze(stm.getExpr());
        analyze(stm.getStms());
    }

    private void analyze(ASTFor stm) {
        for(ASTRE exp : stm.getInit()) {
            analyze(exp);
        }
        analyze(stm.getExpr());
        analyze(stm.getStms());
        for(ASTRE exp : stm.getPost()){
            analyze(exp);
        }
    }

    private void analyze(ASTRE r) {

        if(valid(r)){
            System.out.println("VALID " + r.getCode());
        }

        analyzeNewObj(r);
    }

    private void analyzeNewObj(ASTRE r) {
        if(r != null && r.getExpression() != null) {
            final ASTNewObject[] objs = {null};
            r.getExpression().visit(new DefaultASTVisitor() {
                @Override
                public void enterASTNewObject(ASTNewObject elm) {
                    if(objs[0] == null){
                        objs[0] = elm;
                    }
                }
            });
            if(objs[0] != null)
                analyze(objs[0]);
        }
    }

    private void analyze(IASTStm stm) {
        System.err.println("Not supported, YET!");
    }

    private boolean valid(ASTRE r){
        int s = r.getStart();
        int e = r.getEnd();
        int l = r.getLine();
        boolean flag = false;
        for(IASTStm stm : this.timeStms){
            if(stm.getLine() == l && stm.getStart() == s && stm.getEnd() == e)
                flag = true;
        }
        return flag;
    }
}
