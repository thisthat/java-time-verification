package daikon.parser.structure;

import java.util.List;

public class LOrigArrayVals implements Invariant {

    private String left;
    private List<String> vals;


    public LOrigArrayVals(String left, List<String> vals) {
        this.left = left;
        this.vals = vals;
    }
}
