package daikon.parser.structure;

import java.util.List;

public class ArrayVals implements Invariant {

    private String left;
    private List<String> vals;


    public ArrayVals(String left, List<String> vals) {
        this.left = left;
        this.vals = vals;
    }
}
