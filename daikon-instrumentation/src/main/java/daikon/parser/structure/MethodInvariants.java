package daikon.parser.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodInvariants {
    boolean isClass;
    boolean isObject;
    boolean isEnter;
    boolean isExit;
    int lineExit;
    String method;
    List<String> pars;

    public MethodInvariants(boolean isClass, boolean isObject, boolean isEnter, boolean isExit, int lineExit, String method, List<String> pars) {
        this.isClass = isClass;
        this.isObject = isObject;
        this.isEnter = isEnter;
        this.isExit = isExit;
        this.lineExit = lineExit;
        this.method = method;
        this.pars = pars;
    }

    List<Invariant> invs = new ArrayList<>();

    public void add(Invariant inv){
        invs.add(inv);
    }

    @Override
    public String toString() {
        return "MethodInvariants{" +
                ", method='" + method + '\'' +
                ", size=" + invs.size() +
                '}';
    }
}
