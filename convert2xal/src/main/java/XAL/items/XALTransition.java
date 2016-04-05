package XAL.items;

import XAL.*;
/**
 * Created by Giovanni Liva on 11.02.16.
 */
public class XALTransition  extends XALItem {

    private XALState from;
    private XALState to;

    public XALState getTo() {
        return to;
    }
    public void setTo(XALState to) {
        this.to = to;
    }
    public XALState getFrom() {
        return from;
    }
    public void setFrom(XALState from) {
        this.from = from;
    }

    public  XALTransition(){}
    public  XALTransition(XALState from, XALState to){
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString(int tab){
        String out = "";
        out += tab(tab) + "<Transition IdInputState=\"" + this.from.getID() + "\" IdOutputState=\"" + this.to.getID() + "\" MetricValue=\"failed\" />\n";
        return out;
    }

    @Override
    protected boolean checkConstriant() {
        return false;
    }
}
