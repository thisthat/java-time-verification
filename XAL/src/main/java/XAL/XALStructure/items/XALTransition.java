package XAL.XALStructure.items;

import XAL.XALStructure.XALItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to represent a metric.
 * @TODO support the possibilities to use time constraint as well
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALTransition extends XALItem {

    public static final String METRIC_TRUE = "true";
    public static final String METRIC_FALSE = "false";

    private String from;
    private String to;
	private XALState fromState;
	private XALState toState;
    private String metricValue = null;
    private String style;

    public XALTransition(XALState from, XALState to) {
        this.from = from.getId();
        this.to = to.getId();
		this.fromState = from;
		this.toState = to;
        this.style = "[]";
    }


    public XALTransition(XALState from, XALState to, String metricValue) {
        this.from = from.getId();
        this.to = to.getId();
		this.fromState = from;
		this.toState = to;
        this.metricValue = metricValue;
        this.style = "[]";
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

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString(int tab) {
        String out = tab(tab) + String.format("<Transition IdInputState=\"%s\" IdOutputState=\"%s\" ",this.from, this.to);
        if(this.metricValue != null)
            out += String.format("MetricValue=\"%s\" ",this.metricValue);
        out += String.format("style=\"%s\"", this.style );
		if(!fromState.getTimeConstraint().equals("")){
			out += ">\n";
			out += tab(tab+1) + fromState.getTimeConstraint();
			out += tab(tab) + "</Transition>";
		} else {
			out += " />";
		}
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
