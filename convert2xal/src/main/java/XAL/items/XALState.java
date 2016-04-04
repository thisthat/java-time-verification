package XAL.items;

/**
 * Created by Giovanni Liva on 11.02.16.
 */
public class XALState {
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

    public String toString(int tab){
        String out = "";
        out += XAL.tab(tab) + "<State Id=\"" + this.ID + "\" IdMetric=\"mCHECK\"  style=\"x:79; y:67; h:101; w:128\" />\n";
        return out;
    }
}
