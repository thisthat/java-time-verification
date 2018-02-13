package daikon.parser.structure;

import java.util.ArrayList;
import java.util.List;

public class MethodInvariants {
    boolean isClass;
    boolean isObject;
    boolean isEnter;
    boolean isExit;
    int lineExit;
    String method;
    List<String> pars;

    Boolean isPure = null;
    List<Invariant> invs = new ArrayList<>();

    public MethodInvariants(boolean isClass, boolean isObject, boolean isEnter, boolean isExit, int lineExit, String method, List<String> pars) {
        this.isClass = isClass;
        this.isObject = isObject;
        this.isEnter = isEnter;
        this.isExit = isExit;
        this.lineExit = lineExit;
        this.method = method;
        this.pars = pars;
    }

    void add(Invariant inv) {
        invs.add(inv);
    }

    public boolean isPure(List<String> vars) {
        if (isPure == null) {
            isPure = true;
            for(String p : vars) {
                boolean flag = false;
                for (Invariant i : invs) {
                    if (i instanceof Orig) {
                        Orig o = (Orig) i;
                        if(o.containsVar(p) && o.isSameVar()) {
                            flag = true;
                        }
                    }
                }
                isPure &= flag;
            }
        }
        return isPure;
    }
}
