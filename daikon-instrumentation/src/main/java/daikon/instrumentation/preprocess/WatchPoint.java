package daikon.instrumentation.preprocess;

import java.util.Arrays;
import java.util.Set;

public class WatchPoint {
    String className;
    String methodName;
    int line;
    Set<String> vars;

    public WatchPoint(String className, String methodName, int line, Set<String> vars) {
        this.className = className;
        this.methodName = methodName;
        this.line = line;
        this.vars = vars;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLine() {
        return line;
    }

    public Set<String> getVars() {
        return vars;
    }

    public boolean isTheOne(String className){
        return this.className.equals(className);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%s;%s;%d;%s\n", className, methodName, line, Arrays.toString(vars.toArray())));
        return sb.toString();
    }

    public String printAsMethodDef() {
        StringBuilder name;
        String pars = "";
        name = new StringBuilder("invariant_" + this.line);
        for(String v : vars){
            name.append("_").append(v);
        }
        for(String v : vars){
            pars += "long " + v + ",";
        }
        if(pars.length() > 0) {
            pars = pars.substring(0, pars.length()-1);
        }
        return String.format("public void %s(%s){}", name.toString(), pars);
    }

    public String printAsMethodCall() {
        StringBuilder name;
        String pars = "";
        name = new StringBuilder("invariant_" + this.line);
        for(String v : vars){
            name.append("_").append(v);
        }
        for(String v : vars){
            pars += v + ",";
        }
        if(pars.length() > 0) {
            pars = pars.substring(0, pars.length()-1);
        }
        return String.format("{%s(%s);}", name.toString(), pars);
    }
}
