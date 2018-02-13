package daikon.parser.structure;

public class ROrig implements Invariant {

    private String left;
    private String orig;
    private OP operator;


    public ROrig(String left, OP operator, String orig) {
        this.left = left;
        this.orig = orig;
        this.operator = operator;
    }
}
