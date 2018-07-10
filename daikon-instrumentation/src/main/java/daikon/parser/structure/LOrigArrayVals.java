package daikon.parser.structure;

import java.util.List;

public class LOrigArrayVals implements Invariant {

    private Variable left;
    private List<String> vals;


    public LOrigArrayVals(String left, List<String> vals) {
        this.left = new Variable(left);
        this.vals = vals;
    }

    @Override
    public boolean talks(List<String> vars) {
        return this.left.talks(vars);
    }

    @Override
    public void replace(List<String> vars) {
        this.left.replace(vars);
    }
}
