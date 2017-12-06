package smt;

import com.microsoft.z3.*;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by giovanni on 07/07/2017.
 */
public class ManualExample {

    static {
        String path = "/Users/giovanni/repository/java-xal/correct-time-behavior/src/main/resources/lib";
        System.setProperty("java.library.path", path);
        //set sys_paths to null so that java.library.path will be reevalueted next time it is needed
        final Field sysPathsField;
        try {
            sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static IntExpr timeout;
    private static IntExpr now;
    private static IntExpr deadline;


    private static IntExpr max_val;
    private static IntExpr over_max_val;
    private static IntExpr min_val;
    private static FuncDecl time;

    private static IntExpr timeApp;

    public static void main(String[] args) {

        com.microsoft.z3.Global.ToggleWarningMessages(true);
        Log.open("test.log");

        System.out.print("Z3 Major Version: ");
        System.out.println(Version.getMajor());
        System.out.print("Z3 Full Version: ");
        System.out.println(Version.getFullVersion());
        System.out.print("Z3 Full Version String: ");
        System.out.println(Version.getString());


        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
        Optimize opt = ctx.mkOptimize();

        std(ctx, opt);
        line1(ctx, opt);
        line3(ctx, opt);
        line4(ctx, opt);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        System.err.flush();

//        Status s = opt.Check();
//        System.out.println(s.name());
//        System.out.println(opt.getModel().toString());

    }

    private static void std(Context ctx, Optimize opt) {
        String model =
        "(declare-const max_val Int)\n" +
        "(assert (= max_val 9223372036854775807))\n" +
        "(declare-const over_max_val Int)\n" +
        "(assert (= over_max_val 9223372036854775808))\n" +
        "(declare-const min_val Int)\n" +
        "(assert (= min_val -1))\n" +
        "(declare-fun time () Int)\n" +
        "(assert (= time 0))";
        //BoolExpr std = ctx.parseSMTLIB2String(model, null,null,null,null);
        //opt.Add(std);

        IntExpr maxCorrectUpper = ctx.mkInt("9223372036854775807");
        IntExpr notCorrectUpper = ctx.mkInt("9223372036854775808");

        max_val= ctx.mkIntConst("max_val");
        over_max_val= ctx.mkIntConst("over_max_val");
        min_val= ctx.mkIntConst("min_val");


        BoolExpr std_ok = ctx.mkEq(max_val, maxCorrectUpper);
        BoolExpr std_not = ctx.mkEq(over_max_val, notCorrectUpper);
        opt.Add(std_ok);
        opt.Add(std_not);

        BoolExpr min = ctx.mkEq(min_val, ctx.mkInt(-1));
        opt.Add(min);

        time = ctx.mkFuncDecl("time", new IntSort[0], ctx.getIntSort());
        timeApp = (IntExpr) ctx.mkApp(time, new IntExpr[0]);
        BoolExpr t = ctx.mkEq(timeApp, ctx.mkInt(0));
        opt.Add(t);

    }

    private static void line1(Context ctx, Optimize opt) {
        timeout = ctx.mkIntConst("timeout");
        BoolExpr t = ctx.mkLe(min_val, timeout);
        opt.Add(t);
        t = ctx.mkGe(over_max_val, timeout);
        opt.Add(t);

    }

    private static void line3(Context ctx, Optimize opt) {
        now = ctx.mkIntConst("now");
        BoolExpr t = ctx.mkEq(now, timeApp);
        opt.Add(t);
        t = ctx.mkLe(now, over_max_val);
        opt.Add(t);
        t = ctx.mkGe(now, min_val);
        opt.Add(t);
        Solver solver = ctx.mkSolver();

        solver.push();
        {
            //System.out.println("\nMax Now\n");
            Optimize.Handle mx = opt.MkMaximize(now);
            Status s = opt.Check();
            //System.out.println(s.name());
            System.out.println(opt.getModel().toString());
            //System.out.println("Now has value:" + mx.toString());
            err(mx.toString());

        }
        solver.pop();


        solver.push();
        {
            //System.out.println("\nMin Now\n");
            Optimize.Handle mx = opt.MkMinimize(now);
            Status s = opt.Check();
            //System.out.println(s.name());
            System.out.println(opt.getModel().toString());
            //System.out.println("Now has value:" + mx.toString());
            err(mx.toString());
        }
        solver.pop();



    }

    private static void line4(Context ctx, Optimize opt) {
        deadline = ctx.mkIntConst("deadline");
        BoolExpr t = ctx.mkEq(deadline, ctx.mkAdd(now, timeout));
        opt.Add(t);
        t = ctx.mkLe(deadline, over_max_val);
        opt.Add(t);
        t = ctx.mkGe(deadline, min_val);
        opt.Add(t);
        Solver solver = ctx.mkSolver();

        solver.push();
        {
            //System.out.println("\ndeadline max\n");
            Optimize.Handle mx = opt.MkMaximize(deadline);
            Status s = opt.Check();
            //System.out.println(s.name());
            System.out.println(opt.getModel().toString());
            //System.out.println("deadline has value:" + mx.toString());
            err(mx.toString());
        }
        solver.pop();

        solver.push();
        {
            //System.out.println("\ndeadline min\n");
            Optimize.Handle mx = opt.MkMinimize(deadline);
            Status s = opt.Check();
            //System.out.println(s.name());
            System.out.println(opt.getModel().toString());
            //System.out.println("deadline has value:" + mx.toString());
            err(mx.toString());
            Model m = opt.getModel();
            System.err.append("Statistics\n\n");
            for(Statistics.Entry e : opt.getStatistics().getEntries()){
                System.err.append(e.toString());
                System.err.append("\n");
            }
            for(String st : opt.getStatistics().getKeys()){
                //System.out.println(opt.getStatistics().get(st));
            }
        }
        solver.pop();
    }

    private static void err(String output){
        if( output.equals( "9223372036854775808" )){
            System.out.println("UNSATISFIABLE");
        }
        if( output.equals( "-1" )){
            System.out.println("UNSATISFIABLE");
        }
        System.out.println("-----");
    }

}
