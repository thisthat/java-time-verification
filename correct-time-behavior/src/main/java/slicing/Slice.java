package slicing;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTAssignment;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.structure.expression.ASTVariableDeclaration;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import slicing.heuristics.*;
import slicing.model.*;
import slicing.model.interfaces.Stm;

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
    private List<TimeElement> timeStms;


    public static HashMap<IASTMethod, Method> slice(ASTClass c) {
        TimeStatements timeStms = TimeStatements.getInstance();
        timeStms.clear();
        ah.analyze(c);
        //debug
        Slice slicer = new Slice(c);
        slicer.start();
        return slicer.getSlices();
    }

    private Slice(ASTClass _class) {
        this._class = _class;
        timeStms = TimeStatements.getInstance().getStms();
    }

    public HashMap<IASTMethod, Method> getSlices() {
        return slices;
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
            List<Stm> bck = current;

            for(IASTMethod m : hc.getMethods()) {
                Method mm = new Method(m.getStart(), m.getEnd(), m.getLine(), m.getLineEnd(), m.getCode());
                mm.setName("anonymous" + hc.getIdHidden() + "_" + m.getName());
                mm.setSignature(m.getSignature());
                current = mm.getBody();
                slices.put(m, mm);

                analyze(hc);
            }

            current = bck;
        }
        for(IASTRE p : stm.getParameters()){
            if(p instanceof ASTNewObject){
                analyze((ASTNewObject) p);
            }
        }
    }

    private void analyze(ASTWhile stm) {
        List<Stm> bck = current;
        While w = new While(stm);
        Stm g = getStm(stm.getExpr());
        w.setExpr(g);
        current.add(w);
        current = w.getWhileBody();

        analyze(stm.getStms());

        current = bck;
    }

    private void analyze(ASTDoWhile stm) {

        //one round as it's executed normally
        analyze(stm.getStms());

        List<Stm> bck = current;
        While w = new While(stm);
        Stm g = getStm(stm.getExpr());
        w.setExpr(g);
        current.add(w);
        current = w.getWhileBody();

        analyze(stm.getStms());

        current = bck;

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
        List<Stm> bck = current;
        If _if = new If(stm);
        Stm g = getStm(stm.getGuard());
        _if.setExpr(g);
        current.add(_if);

        current = _if.getIfBody();
        analyze(stm.getIfBranch().getStms());

        if(stm.getElseBranch() != null) {
            current = _if.getElseBody();
            analyze(stm.getElseBranch().getStms());
        }

        current = bck;
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
        Stm stm = getStm(r);
        if(stm != null)
            current.add(stm);
        analyzeNewObj(r);
    }

    private Stm getStm(ASTRE r){
        TimeElement te = valid(r);
        if(te != null){
            IASTRE stm = r.getExpression();
            if(stm instanceof ASTVariableDeclaration || stm instanceof ASTAssignment) {
                return handleAssignment(stm);
            } else if(stm instanceof ASTMethodCall){
                return handleMethodCall((ASTMethodCall)stm);
            }

            Expression e = new Expression(stm);
            e.setExpr(stm);
            return e;
        }
        return null;
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

    private TimeElement valid(ASTRE r){
        int s = r.getStart();
        int e = r.getEnd();
        int l = r.getLine();
        for(TimeElement te : this.timeStms){
            IASTStm stm = te.getStm();
            if(stm.getLine() == l && stm.getStart() == s && stm.getEnd() == e)
                return te;
        }
        return null;
    }

    private Assignment handleAssignment(IASTRE stm) {
        Assignment as = new Assignment(stm);
        if(stm instanceof ASTVariableDeclaration){
            ASTVariableDeclaration vd = (ASTVariableDeclaration) stm;
            as.setLeft(vd.getNameString());
            as.setRight(vd.getExpr());
        } else {
            ASTAssignment a = (ASTAssignment) stm;
            as.setLeft(a.getLeft().print());
            as.setRight(a.getRight());
        }
        return as;
    }

    private MethodCall handleMethodCall(ASTMethodCall stm) {
        MethodCall mc = new MethodCall(stm);
        mc.setPointedClass(stm.getClassPointed());
        mc.setMethodCall(stm);
        return mc;
    }
}
