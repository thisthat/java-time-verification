package daikon.parser.structure;

import java.util.List;

public class Variable implements Invariant {

    private String name;


    public Variable(String name) {
        this.name = name;
    }

    @Override
    public boolean talks(List<String> vars) {
        for(String v: vars){
            if(name.equals(v) || name.equals("this."+v))
                return true;
        }
        return false;
    }

    @Override
    public void replace(List<String> vars) {
        this.name = replace(vars, this.name);
    }

    public String getName() {
        return name;
    }
}
