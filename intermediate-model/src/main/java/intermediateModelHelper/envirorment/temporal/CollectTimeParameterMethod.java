package intermediateModelHelper.envirorment.temporal;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.types.definition.Duration;
import intermediateModel.types.definition.TimeType;
import intermediateModel.types.definition.Timestamp;
import intermediateModel.types.definition.Unknown;
import intermediateModel.types.rules.TimeException;
import intermediateModel.types.rules.TimeTypeError;
import intermediateModel.types.rules.TypeResolver;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.ExtractTimeAttribute;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporalTypes.TemporalTypes;
import intermediateModelHelper.heuristic.v2.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectTimeParameterMethod extends ParseIM {
    static {
        new File("config").mkdir();
    }
    static ApplyHeuristics ah = new ApplyHeuristics();
    static {
        //ah.set__DEBUG__(true);
        ah.subscribe(MarkTime.class);
        ah.subscribe(TimeInSignature.class);
        ah.subscribe(AssignmentTimeVar.class);
        ah.subscribe(BooleanExpression.class);
        ah.subscribe(MinMaxSearch.class);
        ah.subscribe(ReturnExpression.class);
        ah.subscribe(AddTimeVarToTimeExpression.class);
    }

    static TemporalTypes ti = TemporalTypes.getInstance();

    boolean store = false;
    String storeName = "";
    List<TimeMethod> output;
    IASTMethod lastMethod = null;

    public CollectTimeParameterMethod(ASTClass _class, boolean store, String storeName) {
        super(_class);
        this.store = store;
        this.storeName = storeName;
    }

    public CollectTimeParameterMethod(boolean store, String storeName) {
        this.store = store;
        this.storeName = storeName;
    }

    @Override
    protected void analyzeMethod(IASTMethod method, Env e) {
        IASTMethod bck = lastMethod;
        lastMethod = method;
        super.analyzeMethod(method, e);
        lastMethod = bck;
    }

    public List<TimeMethod> index(ASTClass c){
        ah.analyze(c);
        output = new ArrayList<>();
        super.set_class(c);
        Env base = super.createBaseEnv(c);
        ExtractTimeAttribute timeAttribute = new ExtractTimeAttribute(c);
        for(IASTVar p : timeAttribute.getTimeAttributes()){
            if(base.existVarName(p.getName())){
                IASTVar v = base.getVar(p.getName());
                v.setTimeCritical(true);
            }
        }
        //first constructor
        for(IASTMethod m : c.getMethods()){
            if(m instanceof ASTMethod) continue;
            Env eMethod = new Env(base);
            eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
            analyzeMethod(m, eMethod);
            //analyze(m.getStms(), eMethod );
        }

        //then methods
        for(IASTMethod m : c.getMethods()){
            if(m instanceof ASTConstructor) continue;
            //String ret = m.getReturnType();
            //if(ret.equals("long") || ret.equals("int") || ret.equals("Long") || ret.equals("Integer")) {
                Env eMethod = new Env(base);
                eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
                analyzeMethod(m, eMethod);
            //}
            //analyze(m.getStms(), eMethod );
        }
        List<TimeMethod> outputT = new ArrayList<>();
        List<TimeMethod> outputD = new ArrayList<>();
        for(TimeMethod t : output){
            if(t.getTimeType() instanceof Timestamp){
                outputT.add(t);
            } else if(t.getTimeType() instanceof Duration){
                outputD.add(t);
            }
        }
        if(store){
            String full = "config/" + this.storeName + "_methods.csv";
            String timestamp = "config/" + this.storeName + "_et_t.csv";
            String duration = "config/" + this.storeName + "_et_d.csv";
            try {
                writeFile(full, this.output);
            } catch (IOException e) {
                System.err.println("Cannot write " + full + " file");
                System.err.println(e.getMessage());
            }
            try {
                writeFile(timestamp, outputT);
                ti.loadUserTypes_ETT(timestamp);
            } catch (IOException e) {
                System.err.println("Cannot write " + timestamp + " file");
                System.err.println(e.getMessage());
            }
            try {
                writeFile(duration, outputD);
                ti.loadUserTypes_ETD(duration);
            } catch (IOException e) {
                System.err.println("Cannot write " + duration + " file");
                System.err.println(e.getMessage());
            }
        }
        return output;
    }

    private void writeFile(String filename, List<TimeMethod> output) throws IOException{
        File f = new File(filename);
        boolean exists = f.exists();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
        if(!exists)
            writer.write("Class;Name;Signature;Ret Type\n");
        for(TimeMethod t : output){
            writer.write(t.toString());
            writer.write("\n");
            writer.flush();
        }
        writer.close();
    }

    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        super.analyzeASTRE(r,env);
        CheckExpression.checkRE(r, env);
        if(r != null && r.getExpression() != null) {
            try {
                TypeResolver.resolveTimerType(r.getExpression(), env);
            } catch (TimeException timeTypeError) {
                // no care for errors
            }
        }
    }

    @Override
    protected void postAnalyzeASTMethod(IASTMethod elm, Env env) {
        for(int i = 0; i < elm.getParameters().size(); i++){
            ASTVariable v = elm.getParameters().get(i);
            if(v.isTimeCritical()){
                TimeType tt = v.getVarTimeType();
                if(tt != null && !(tt instanceof Unknown)){
                    TimeMethod t = new TimeMethod(_class.fullName(), lastMethod.getName(), lastMethod.getSignature(),
                            new int[] {i}, tt);
                    if (!output.contains(t))
                        output.add(t);
                }
            }
        }
    }
}
