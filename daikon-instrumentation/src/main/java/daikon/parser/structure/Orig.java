package daikon.parser.structure;

public class Orig implements Invariant {
    private String orig;
    private String right;
    private OP operator;


    public Orig(String orig, OP operator, String right) {
        this.orig = orig.trim();
        this.right = right.trim();
        this.operator = operator;
    }

    public boolean isSameVar(){
        return orig.equals(right);
    }

    public boolean containsVar(String p) {
        //remove this. from all
        String pp = p.replace("this.", "");
        String oo = orig.replace("this.", "");
        String rr = right.replace("this.", "");
        return pp.equals(oo) || pp.equals(rr);
    }
}
