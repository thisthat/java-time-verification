package XAL.XALStructure.items;

import XAL.XALStructure.XALItem;

/**
 * Class used to represent a XAL state.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class XALState extends XALItem {

    protected String id;
    protected String idAction = null;
    protected String idMetric = null;
    protected String style = "x:31; y:44; h:30; w:49";
    protected String timeConstraint = "";

    /**
     * Enumeration that allows the constructor to chose between action or metric
     */
    public enum TypeProduction {
        ACTION,
        METRIC
    }

    /**
     * Construct a XALState object with only the ID
     * @param id The id of the state
     */
    public XALState(String id) {
        this.id = id;
    }

    /**
     * Construct a XALState object with an action or a metric. In order to decide which one use, the {@link TypeProduction} parameter is used.
     * @param id                name of the state
     * @param id_action_metric  name of the action/metric
     * @param production        ACTION | METRIC
     */
    public XALState(String id, String id_action_metric, TypeProduction production) {
        this.id = id;
        if(production == TypeProduction.ACTION)
            this.idAction = id_action_metric;
        else
            this.idMetric = id_action_metric;
    }

    /**
     * Construct a XALState object with an action and a metric.
     * @param id        name of the state
     * @param idAction  name of the action
     * @param idMetric  name of the metric
     */
    public XALState(String id, String idAction, String idMetric) {
        this.id = id;
        this.idAction = idAction;
        this.idMetric = idMetric;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdAction() {
        return idAction;
    }

    public String getIdMetric() {
        return idMetric;
    }

    public String getTimeConstraint() {
        return timeConstraint;
    }

    public void setTimeConstraint(String timeConstraint) {
        this.timeConstraint = timeConstraint;
    }

    @Override
    public String toString(int tab) {
        String out = "";
        out += tab(tab) + String.format("<State Id=\"%s\" ", this.id);
        if(idMetric != null)
            out += String.format("IdMetric=\"%s\" ", this.idMetric);
        if(idAction != null)
            out += String.format("IdAction=\"%s\" ", this.idAction);
        if(style != null)
            out += String.format("style=\"%s\" ", this.style);
        out += "/>";
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
        return true;
    }

}
