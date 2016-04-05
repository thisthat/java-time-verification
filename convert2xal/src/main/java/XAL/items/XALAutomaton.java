package XAL.items;

import java.util.ArrayList;
import java.util.List;

import XAL.*;

/**
 * Xal Automaton. This class represent the structure of a single automaton.
 * It stores information about
 * <ul>
 * <li>{@link XALGlobalState}</li>
 * <li>{@link XALClock}</li>
 * <li>{@link XALActionPool}</li>
 * <li>{@link XALState}</li>
 * <li>{@link XALInitialState}</li>
 * <li>{@link XALFinalStates}</li>
 * <li>{@link XALTransition}</li>
 * </ul>
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class XALAutomaton extends XALItem {
    private static int __counter = 0;

    private XALGlobalState GlobalState;
    private String ID;
    private String Clocks;
    private String ActionPool;
    private List<XALState> States;
    private XALState InitialState;
    private XALState FinalStates;
    private List<XALTransition> Transitions;


    public XALAutomaton() {
        States = new ArrayList<XALState>();
        Transitions = new ArrayList<XALTransition>();
        ID = "NoName_" + __counter++;
    }

    public XALAutomaton(String id) {
        this.States = new ArrayList<XALState>();
        this.Transitions = new ArrayList<XALTransition>();
        this.ID = id;
    }

    public XALGlobalState getGlobalState() {
        return GlobalState;
    }

    public void setGlobalState(XALGlobalState globalState) {
        GlobalState = globalState;
    }


    public void addState(XALState s) {
        this.States.add(s);
    }

    public void addTransition(XALTransition t) {
        this.Transitions.add(t);
    }

    @Override
    public String toString(int tab) {
        String out = "";
        out += tab(tab) + "<Automaton Id=\"" + this.ID + "\">\n";
        out += tab(tab + 1) + "<GlobalState/>\n";
        out += tab(tab + 1) + "<Clocks/>\n";
        out += tab(tab + 1) + "<ActionPool>\n";
        out += tab(tab + 2) + XALMetrics.stub_metric;
        out += tab(tab + 1) + "</ActionPool>\n";
        out += tab(tab + 1) + "<States>\n";
        for (XALState s : this.States) {
            out += s.toString(tab + 2);
        }
        out += tab(tab + 1) + "</States>\n";
        String initState = this.States.get(0).getID();
        out += tab(tab + 1) + "<InitialState IdState=\"" + initState + "\"/>\n";
        out += tab(tab + 1) + "<FinalStates/>\n";
        out += tab(tab + 1) + "<Transitions>\n";
        for (XALTransition t : this.Transitions) {
            out += t.toString(tab + 2);
        }
        out += tab(tab + 1) + "</Transitions>\n";
        out += tab(tab) + "</Automaton>\n";
        return out;
    }

    /**
     * No constraint to check
     *
     * @return Always true
     */
    @Override
    protected boolean checkConstriant() {
        return true;
    }
}
