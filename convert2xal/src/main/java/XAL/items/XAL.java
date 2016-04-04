package XAL.items;

import XAL.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Still in coding</b>
 * Root element of a XAL document. It handles a whole XAL file.
 * TODO: better separation of the task and enhance the possibilities of the class with more feature
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class XAL extends XALItem {

    private List<XALAutomaton> automatons = new ArrayList<XALAutomaton>();

    public XAL(){}

    public void addAutomaton(XALAutomaton a){
        automatons.add(a);
    }

    public void toFile(){
        try {
            PrintWriter out = new PrintWriter("trace.xal");
            out.println(this.toString());
            out.close();
        } catch (FileNotFoundException e) {
            //No file :(
            System.err.println("Error in generating XAL file");
            return;
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

}
