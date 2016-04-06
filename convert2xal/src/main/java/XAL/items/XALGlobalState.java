package XAL.items;

import XAL.XALItem;

import java.util.ArrayList;
import java.util.List;

/**
 * It contains all the {@link XALVariable} of an automaton.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALGlobalState extends XALItem {

    private List<XALVariable> variables;


    public XALGlobalState(){
       this.variables = new ArrayList<XALVariable>();
    }

    public XALGlobalState(List<XALVariable> variables) {
        this.variables = variables;
    }

    public List<XALVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<XALVariable> variables) {
        this.variables = variables;
    }

    public void addVariable(XALVariable v){
        variables.add(v);
    }

    @Override
    public String toString(int tab) {
        String out = "";
        out += tab(tab) + "Var: [\n";
        for (XALVariable v: this.variables ) {
            out += v.toString(2) + "\n";
        }
        out += "\n]\n";
        return out;
    }

    /**
     * No constraint to check
     * @return Always true
     */
    @Override
    protected boolean checkConstriant() {
        return true;
    }
}
