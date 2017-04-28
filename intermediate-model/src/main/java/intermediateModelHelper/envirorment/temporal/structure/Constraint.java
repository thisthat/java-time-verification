package intermediateModelHelper.envirorment.temporal.structure;

import intermediateModel.interfaces.IASTStm;
import intermediateModelHelper.heuristic.definition.SearchTimeConstraint;

/**
 * Created by giovanni on 22/03/2017.
 */
public class Constraint {
    IASTStm elm;
    String category;
    String value;
    int line;
    Constraint edgeVersion;

    public Constraint(IASTStm elm, String category, String value, int line) {
        this.elm = elm;
        this.category = category;
        this.value = value;
        this.line = line;
    }

    public Constraint(IASTStm elm, Class category, String value, int line) {
        this(elm, category.getCanonicalName(), value, line);
    }

    public String getCategory() {
        return category;
    }

    public String getValue() {
        if(value.startsWith(".")){
            String tmp = value.substring(1); //no first dot
            int first = tmp.indexOf("(");
            int last = tmp.lastIndexOf(")");
            String firstPart = tmp.substring(0, first);
            String lastPart = tmp.substring(last + 1);
            return firstPart+lastPart;
        }
        return value;
    }

    public int getLine() {
        return line;
    }

    public IASTStm getElm() {
        return elm;
    }

    public void setEdgeVersion(Constraint edgeVersion) {
        this.edgeVersion = edgeVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint that = (Constraint) o;

        if (line != that.line) return false;
        if (elm != null ? !elm.equals(that.elm) : that.elm != null) return false;
        if (category != null ? !category.equals(that.category) : that.category != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int result = elm != null ? elm.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + line;
        return result;
    }

    public boolean isCategory(Class<? extends SearchTimeConstraint> _class){
        return this.category.equals(_class.getCanonicalName());
    }

    public Constraint negate() {
        String neg_value = this.value;
        if(neg_value.contains("<="))
            neg_value = neg_value.replace("<=", ">");
        else if(neg_value.contains(">="))
            neg_value = neg_value.replace(">=", "<");
        else if(neg_value.contains("=="))
            neg_value = neg_value.replace("==", "<") + " || " + neg_value.replace("==", ">");
        else if(neg_value.contains("!="))
            neg_value = neg_value.replace("!=", "==");
        else if(neg_value.contains("<"))
            neg_value = neg_value.replace("<", ">=");
        else if(neg_value.contains(">"))
            neg_value = neg_value.replace(">", "<=");
        return new Constraint(this.elm, this.category, neg_value, this.line);
    }

    public Constraint getEdgeVersion(){
        return edgeVersion;
    }
}
