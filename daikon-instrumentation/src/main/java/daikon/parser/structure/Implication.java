package daikon.parser.structure;

public class Implication implements Invariant {

    private Invariant left;
    private Invariant right;


    public Implication(Invariant left, Invariant right) {
        this.left = left;
        this.right = right;
    }
}
