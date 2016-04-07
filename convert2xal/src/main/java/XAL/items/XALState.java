package XAL.items;
import XAL.*;

/**
 * Class used to represent a state.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class XALState extends XALItem {

    private String id;
    private String idAction = null;
    private String idMetric = null;
    private String style;

    public enum TypeProduction {
        ACTION,
        METRIC
    }

    public XALState(String id) {
        this.id = id;
    }

    public XALState(String id, String id_action_metric, TypeProduction production) {
        this.id = id;
        if(production == TypeProduction.ACTION)
            this.idAction = id_action_metric;
        else
            this.idMetric = id_action_metric;
    }

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

    public String getIdAction() {
        return idAction;
    }

    public String getIdMetric() {
        return idMetric;
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
        out += ">";
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
