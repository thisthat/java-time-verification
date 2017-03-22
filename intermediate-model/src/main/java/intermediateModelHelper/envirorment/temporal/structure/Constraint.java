package intermediateModelHelper.envirorment.temporal.structure;

import intermediateModel.interfaces.IASTStm;

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
}
