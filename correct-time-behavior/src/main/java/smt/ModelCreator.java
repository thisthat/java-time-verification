package smt;

import com.microsoft.z3.*;
import smt.exception.ModelNotCorrect;
import smt.exception.VarNotFoundException;

import java.util.HashMap;

/**
 * Created by giovanni on 10/07/2017.
 */
public class ModelCreator {

    private IntExpr max_val;
    private IntExpr over_max_val;
    private IntExpr min_val;
    private IntExpr time;
    private FuncDecl timeFuncDec;

    public static String _MaxVal = "9223372036854775807";
    public static String _NotValidMax = "9223372036854775808";
    public static String _NotValidMin = "-1";

    Context ctx;
    Optimize opt;

    HashMap<String,IntExpr> vars = new HashMap<>();

    public ModelCreator() {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        ctx = new Context(cfg);
        opt = ctx.mkOptimize();
        insertStandardDefinition();
    }

    private void insertStandardDefinition(){
        IntExpr maxCorrectUpper = ctx.mkInt( _MaxVal );
        IntExpr notCorrectUpper = ctx.mkInt(_NotValidMax);

        max_val= ctx.mkIntConst("max_val");
        over_max_val= ctx.mkIntConst("over_max_val");
        min_val= ctx.mkIntConst("min_val");

        //Bind every var/fun to its value and correct range

        BoolExpr std_ok = ctx.mkEq(max_val, maxCorrectUpper);
        BoolExpr std_not = ctx.mkEq(over_max_val, notCorrectUpper);
        opt.Add(std_ok);
        opt.Add(std_not);

        BoolExpr min = ctx.mkEq(min_val, ctx.mkInt(_NotValidMin));
        opt.Add(min);

        timeFuncDec = ctx.mkFuncDecl("time", new IntSort[0], ctx.getIntSort());
        time = (IntExpr) ctx.mkApp(timeFuncDec, new IntExpr[0]);
        BoolExpr t = ctx.mkEq(time, ctx.mkInt(0));
        opt.Add(t);
    }


    public IntExpr createVariable(String name){
        IntExpr v = ctx.mkIntConst(name);
        BoolExpr t = ctx.mkLe(min_val, v);
        opt.Add(t);
        t = ctx.mkGe(over_max_val, v);
        opt.Add(t);
        vars.put(name, v);
        return v;
    }

    public IntExpr getVar(String name) throws VarNotFoundException {
        if(!vars.containsKey(name)){
            throw new VarNotFoundException();
        }
        return vars.get(name);
    }

    public void verifyVariable(String name) throws ModelNotCorrect, VarNotFoundException {
        IntExpr v = this.getVar(name);
        this.verifyVariable(v);
    }

    public void verifyVariable(IntExpr v) throws ModelNotCorrect {
        verify_min(v);
        verify_max(v);
    }

    private void verify_max(IntExpr v) throws ModelNotCorrect {
        opt.Push();
        Optimize.Handle mx = opt.MkMaximize(v);
        opt.Check();
        boolean f = validValue(mx.toString());
        opt.Pop();
        if(!f){
            throw new ModelNotCorrect();
        }
    }

    private void verify_min(IntExpr v) throws ModelNotCorrect {
        opt.Push();
        Optimize.Handle mx = opt.MkMinimize(v);
        opt.Check();
        boolean f = validValue(mx.toString());
        opt.Pop();
        if(!f){
            throw new ModelNotCorrect();
        }
    }

    private boolean validValue(String val){
        if( val.equals( _NotValidMax )){
            return false;
        }
        if( val.equals( _NotValidMin )){
            return false;
        }
        return true;
    }

    public void addConstraint(BoolExpr t){
        opt.Add(t);
    }

    public Model getModel() {
        opt.Check();
        return opt.getModel();
    }

    public Context getCtx() {
        return ctx;
    }

    public Optimize getOpt() {
        return opt;
    }
}
