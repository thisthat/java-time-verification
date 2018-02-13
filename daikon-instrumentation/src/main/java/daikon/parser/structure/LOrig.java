package daikon.parser.structure;

public class LOrig implements Invariant {

    private String orig;
    private String right;
    private OP operator;


    public LOrig(String orig, OP operator, String right) {
        this.orig = orig;
        this.right = right;
        this.operator = operator;
    }
}
