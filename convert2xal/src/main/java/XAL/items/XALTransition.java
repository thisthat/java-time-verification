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
    private String metricValue;

    public XALTransition(XALState from, XALState to, String metricValue) {
        this.from = from.getId();
        this.to = to.getId();
        this.metricValue = metricValue;
    }

    public XALTransition(String from, String to, String metricValue) {
        this.from = from;
        this.to = to;
        this.metricValue = metricValue;
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
        return "";
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
