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
        return value;
    }

    public int getLine() {
        return line;
    }

    public IASTStm getElm() {
        return elm;
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
}
