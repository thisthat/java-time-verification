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
        return "";
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
