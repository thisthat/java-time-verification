package XAL.items;
import XAL.*;

/**
 * Created by Giovanni Liva on 11.02.16.
 */

public class XALState extends XALItem {
    private static int __COUNTER = 0;
    private String ID;
    private String IDMetric;

    public XALState(String name){
        this.ID = name + __COUNTER++;
        this.IDMetric = XALMetrics.stub_metric_name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getIDMetric() {
        return IDMetric;
    }

    public void setIDMetric(String IDMetric) {
        this.IDMetric = IDMetric;
    }

    @Override
    public String toString(int tab){
        String out = "";
        out += tab(tab) + "<State Id=\"" + this.ID + "\" IdMetric=\"mCHECK\"  style=\"x:79; y:67; h:101; w:128\" />\n";
        return out;
    }

    @Override
    protected boolean checkConstriant() {
        return false;
    }
}
