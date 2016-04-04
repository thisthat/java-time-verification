package XAL.items;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giovanni Liva on 11.02.16.
 */
public class XALAutomaton {
    private String GlobalState;
    private String ID;
    private String Clocks;
    private String ActionPool;
    private List<XALState> States;
    private XALState InitialState;
    private XALState FinalStates;
    private List<XALTransition> Transitions;


    public XALAutomaton(){
        States = new ArrayList<>();
        Transitions = new ArrayList<>();
        ID = "SingleXAL_Automata";
    }

    public void addState(XALState s){
        this.States.add(s);
    }

    public void addTransition(XALTransition t) {
        this.Transitions.add(t);
    }


    public String toString(int tab){
        String out = "";
        out += XAL.tab(tab) + "<Automaton Id=\"" + this.ID + "\">\n";
        out += XAL.tab(tab+1) + "<GlobalState/>\n";
        out += XAL.tab(tab+1) + "<Clocks/>\n";
        out += XAL.tab(tab+1) + "<ActionPool>\n";
        out += XAL.tab(tab+2) + XALMetrics.stub_metric;
        out += XAL.tab(tab+1) + "</ActionPool>\n";
        out += XAL.tab(tab+1) + "<States>\n";
        for(XALState s : this.States){
            out += s.toString(tab+2);
        }
        out += XAL.tab(tab+1) + "</States>\n";
        String initState = this.States.get(0).getID();
        out += XAL.tab(tab+1) + "<InitialState IdState=\"" + initState + "\"/>\n";
        out += XAL.tab(tab+1) + "<FinalStates/>\n";
        out += XAL.tab(tab+1) + "<Transitions>\n";
        for(XALTransition t : this.Transitions){
            out += t.toString(tab+2);
        }
        out += XAL.tab(tab+1) + "</Transitions>\n";
        out += XAL.tab(tab) + "</Automaton>\n";
        return out;
    }
}
