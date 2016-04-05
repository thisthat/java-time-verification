package XAL.items;

import java.util.ArrayList;
import java.util.List;

import XAL.*;
import XAL.exception.XALMalformedException;

/**
 * Xal Automaton. This class represent the structure of a single automaton.
 * It stores information about
 * <ul>
 * <li>{@link XALGlobalState} :: List of variables</li>
 * <li>{@link XALClock} :: List of clock variables</li>
 * <li>{@link XALActionPool} :: List of action and metrics @see "{@link XALAction} and {@link XALMetric}"</li>
 * <li>{@link XALState} :: List of states</li>
 * <li>Initial State</li>
 * <li>List of end States</li>
 * <li>{@link XALTransition} :: List of transition between {@link XALState}</li>
 * </ul>
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class XALAutomaton extends XALItem {
    private static int __counter = 0;
    public static final String __HIDDEN_AUTOMATA__ = "NoName_";

    private XALGlobalState globalState;
    private String id;
    private String clocks;
    private String actionPool;
    private List<XALState> states;
    private String initialState;
    private List<String> finalStates;
    private List<XALTransition> transitions;



    public XALAutomaton() {
        this.states = new ArrayList<XALState>();
        this.transitions = new ArrayList<XALTransition>();
        this.finalStates = new ArrayList<String>();
        this.id = __HIDDEN_AUTOMATA__ + __counter++;
    }

    public XALAutomaton(String id) {
        this.states = new ArrayList<XALState>();
        this.transitions = new ArrayList<XALTransition>();
        this.finalStates = new ArrayList<String>();
        this.id = id;
    }

    /**
     * Get the {@link XALGlobalState} of the automata
     * @return the list of variables of the automata
     */
    public XALGlobalState getGlobalState() {
        return globalState;
    }
    /**
     * Set the {@link XALGlobalState} of the automata
     * @param globalState list of variables for the automata
     */
    public void setGlobalState(XALGlobalState globalState) {
        this.globalState = globalState;
    }


    /**
     * return the list of states inside the automata
     * @return The list of states in the automata
     */
    public List<XALState> getStates() {
        return states;
    }

    /**
     * Set the list of states of the automata
     * @param states    List of {@link XALState} to uses as automaton states
     */
    public void setStates(List<XALState> states) {
        this.states = states;
    }

    /**
     * Add a {@link XALState} to the list of states of the automata
     * @param s {@link XALState} to add
     */
    public void addState(XALState s) {
        this.states.add(s);
    }


    /**
     * Get the id of the init state of the automaton
     * @return the String that correspond to the ID of the {@link XALState}
     */
    public String getInitialState() {
        return initialState;
    }

    /**
     * Set the ID of the initial state of the automaton
     * @param state {@link XALState} to use as initial state
     * @throws XALMalformedException when the id passed as parameter does not exist in the list of state of the automaton
     */
    public void setInitialState(XALState state) throws XALMalformedException {
        setInitialState(state.getId());
    }

    /**
     * Set the ID of the initial state of the automaton
     * @param initialState  String that represent the ID of the {@link XALState} to use as initial state
     * @throws  XALMalformedException when the id passed as parameter does not exist in the list of state of the automaton
     */
    public void setInitialState(final String initialState) throws XALMalformedException {
        if(this.states.stream().anyMatch( s -> (s.getId() == initialState) )  ){
            throw new XALMalformedException("Initial state ID not found in the list of states");
        }
        this.initialState = initialState;
    }


    /**
     * return the list of final states ids inside the automaton.
     * @return The list of ids corresponding to the final {@link XALState} of the automaton.
     */
    public List<String> getFinalStates() {
        return finalStates;
    }

    /**
     * Set the list of final states of the automaton
     * @param states    List of ids of the corresponding {@link XALState} to use as final state
     */
    public void setFinalStates(List<String> states) {
        this.finalStates = states;
    }

    /**
     * Add an id to the list of final states of the automaton
     * @param s id to add
     */
    public void addFinalState(String s) {
        this.finalStates.add(s);
    }


    /**
     * Add the id of the state to the list of final states of the automaton
     * @param s {@link XALState} to add
     */
    public void addFinalState(XALState s) {
        this.finalStates.add(s.getId());
    }


    /**
     * Get all the transition of the automaton
     * @return List of {@link XALTransition}
     */
    public List<XALTransition> getTransitions() {
        return transitions;
    }

    /**
     * Set the list of automaton transition
     * @param transitions   list of {@link XALTransition} to use
     */
    public void setTransitions(List<XALTransition> transitions) {
        this.transitions = transitions;
    }

    /**
     * Add a single {@link XALTransition} to the list of transiiton
     * @param t     {@link XALTransition} to add
     */
    public void addTransition(XALTransition t) {
        this.transitions.add(t);
    }



    @Override
    public String toString(int tab) {
        String out = "";
        return out;
    }

    /**
     * Check if the transition names are inside the metric enumeration
     * @return Always true
     */
    @Override
    protected boolean checkConstriant() {
        return true;
    }
}
