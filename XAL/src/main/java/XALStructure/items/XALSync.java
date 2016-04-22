package XALStructure.items;

import XALStructure.XALItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to represent a XAL state.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class XALSync extends XALState {


    List<XALState> states = new ArrayList<XALState>();

    /**
     * Check the super method in {@link XALState}
     * @param id The id of the state
     */
    public XALSync(String id) {
        super(id);
    }

    /**
     * Check the super method {@link XALState}
     * @param id                name of the state
     * @param id_action_metric  name of the action/metric
     * @param production        ACTION | METRIC
     */
    public XALSync(String id, String id_action_metric, TypeProduction production) {
        super(id,id_action_metric, production);
    }

    /**
     * Check the super method {@link XALState}
     * @param id        name of the state
     * @param idAction  name of the action
     * @param idMetric  name of the metric
     */
    public XALSync(String id, String idAction, String idMetric) {
        super(id,idAction,idMetric);
    }

    /**
     * Add a state to the list of states inside a synchronized block
     * @param state The {@link XALState} to add
     */
    public void addState(XALState state){
        states.add(state);
    }

    /**
     * Get the list of states inside the synchronized block
     * @return  A list of {@link XALState}
     */
    public List<XALState> getStates() {
        return states;
    }

    @Override
    public String toString(int tab) {
        String out = "";
        out += tab(tab) + String.format("<sync Id=\"%s\" ", this.id);
        if(style != null)
            out += String.format("style=\"%s\" ", this.style);
        out += ">\n";
        for (XALState v: this.states ) {
            out += v.toString(tab+1) + "\n";
        }
        out += tab(tab) + "</sync>";
        return out;
    }

    @Override
    public String toString(){
        return this.toString(0);
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
