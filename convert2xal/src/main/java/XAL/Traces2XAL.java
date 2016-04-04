package XAL;

import jar.ExeFinishListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import XAL.items.*;

/**
 * Created by Giovanni Liva on 11.02.16.
 */

/**
 * This class converts traces to XAL files (in a wrong and trivial way)
 * It uses ExeFinishListener to run the convertion after the traces are collected
 *
 * @see         ExeFinishListener
 * @author      Giovanni Liva
 * @version     %I%, %G%
 */
public class Traces2XAL implements ExeFinishListener {

    private List<String> methods;
    private ArrayList<XALEvents> listeners = new ArrayList<XALEvents>();


    /**
     * Constructor of the class. It initialize the list for the listeners for XALEvents
     *
     * @see             XALEvents
     * @since           1.0
     */
    public Traces2XAL(){
        methods = new ArrayList<String>();
    }

    /**
     * Add a new listener for XALEvents
     *
     * @param toAdd     The object that listen to the XALEvents
     * @see             XALEvents
     * @since           1.0
     */
    public void addListenerOnEvents(XALEvents toAdd){
        listeners.add(toAdd);
    }


    /**
     * This method intercept the execution of the jar event and receive the traces.
     * When the traces arrived, this method spawn a thread that convert to a XAL files them
     *
     * @param traces    The list of methods called
     * @since           1.0
     */
    public void OnExeFinished(List<String> traces) {
        for(String t : traces){
            String[] exp = t.split("--");
            //We take only the name of the method
            methods.add(exp[0]);
        }
        sendStartEvent();
        Thread a = new Thread(() -> {
            ConvertMethodsToXAL();
        });
        a.start();
    }

    private void ConvertMethodsToXAL(){
        XAL xal = new XAL();
        XALAutomaton automaton = new XALAutomaton();
        xal.addAutomaton(automaton);
        XALState old = null;
        //Create the states
        for(String m : methods){
            XALState s = new XALState(m);
            automaton.addState(s);
            //Create the transition
            if(old != null){
                XALTransition t = new XALTransition(old, s);
                automaton.addTransition(t);
            }
            old = s;
        }
        xal.toFile();
        System.out.println("File Generated");
        //Send end event
        sendFinishEvent();
    }


    private void sendStartEvent(){
        for(XALEvents x : listeners){
            x.OnGeneratingStart();
        }
    }
    private void sendFinishEvent(){
        for(XALEvents x : listeners){
            x.OnGeneratingFinished();
        }
    }


}
