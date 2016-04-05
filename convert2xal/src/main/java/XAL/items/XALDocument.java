package XAL.items;

import XAL.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Root element of a XAL document. It handles a whole XAL file.
 * The task of the class is just to collect the list of automaton that are inside a XAL file.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class XALDocument extends XALItem {

    private List<XALAutomaton> automatons = new ArrayList<XALAutomaton>();

    public XALDocument(){}
    public XALDocument(List<XALAutomaton> l){
        this.automatons = l;
    }
    public XALDocument(XALAutomaton a){
        this.automatons.add(a);
    }

    public void addAutomaton(XALAutomaton a){
        automatons.add(a);
    }
    public List<XALAutomaton> getAutomatons() {
        return automatons;
    }


    /**
     * It generate the xal representation in a file, called <i>trace.xal</i>, on the current working path.
     */
    public void toFile() {
        toFile("trace.xal");
    }

    /**
     * It generate the xal representation in the filepath passed as parameter.
     * @param filename  The path of where store the XAL file.
     */
    public void toFile(String filename){
        try {
            PrintWriter out = new PrintWriter(filename);
            out.println(this.toString());
            out.close();
        } catch (Exception e) {
            //If a system IO error occurs, it is not a problem. Just report that it happened.
            System.err.println("Error in generating XAL file");
        }
    }

    @Override
    public String toString(){
        String out = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
        out += "<XAL>\n";
        for(XALAutomaton a : this.automatons){
            out += a.toString(1);
        }
        out += "</XAL>";
        return out;
    }

    public String toString(int tab) {
        return null;
    }

    @Override
    protected boolean checkConstriant() {
        return false;
    }
}
