package daikon.parser.structure;

public class Size implements Invariant {

    private String left;
    private String right;
    private OP operator;


    public Size(String left, String right, OP operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
}
