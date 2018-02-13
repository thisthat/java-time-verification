package daikon.parser.structure;

public class Check implements Invariant {

    private String left;
    private String right;
    private OP operator;


    public Check(String left, String right, OP operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
}
