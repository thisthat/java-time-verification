package daikon.parser.structure;

public class Mod implements Invariant {

    private String var;
    private String mod;


    public Mod(String var, String mod) {
        this.var = var;
        this.mod = mod;
    }
}
