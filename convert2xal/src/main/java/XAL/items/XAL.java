package XAL.items;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giovanni Liva on 11.02.16.
 */
public class XAL {

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

    public static String tab(int n){
        if(n == 0)
            return "";
        else
            return "\t" + tab(n-1);
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
