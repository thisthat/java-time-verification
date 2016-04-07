package XAL.items;

import XAL.*;
/**
 * Class used to represent a metric.
 * @TODO support the possibilities to use time constraint as well
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALTransition extends XALItem {

    private String from;
    private String to;
    private String metricValue = null;
    private String style;

    public XALTransition(XALState from, XALState to) {
        this.from = from.getId();
        this.to = to.getId();
        this.style = "[]";
    }

    public XALTransition(String from, String to) {
        this.from = from;
        this.to = to;
        this.style = "[]";
    }

    public XALTransition(XALState from, XALState to, String metricValue) {
        this.from = from.getId();
        this.to = to.getId();
        this.metricValue = metricValue;
        this.style = "[]";
    }

    public XALTransition(String from, String to, String metricValue) {
        this.from = from;
        this.to = to;
        this.metricValue = metricValue;
        this.style = "[]";
    }

    public XALTransition(XALState from, XALState to, String metricValue, String style) {
        this.from = from.getId();
        this.to = to.getId();
        this.metricValue = metricValue;
        this.style = style;
    }

    public XALTransition(String from, String to, String metricValue, String style) {
        this.from = from;
        this.to = to;
        this.metricValue = metricValue;
        this.style = style;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public String toString(int tab) {
        String out = String.format("<Transition IdInputState=\"%s\" IdOutputState=\"%d\" ",this.from, this.to);
        if(this.metricValue != null)
            out += String.format("MetricValue=\"%s\" ",this.metricValue);
        out += String.format("style=\"%s\" />", this.style );
        return out;
    }


    @Override
    public String toString(){
        return this.toString(0);
    }
    /**
     * No constraint to check
     * @return Always true
     */
    @Override
    protected boolean checkConstriant() {
        return false;
    }
}
