package daikon.parser.structure;

import java.util.List;

public class OneOf implements Invariant {

    private String left;
    private List<String> elms;

    public OneOf(String left, List<String> elms) {
        this.left = left;
        this.elms = elms;
    }
}
