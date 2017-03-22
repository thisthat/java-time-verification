package intermediateModelHelper.envirorment.temporal.structure;

/**
 * Created by giovanni on 22/03/2017.
 */
public class Constraint {
    String category;
    String value;
    int line;

    public Constraint(String category, String value, int line) {
        this.category = category;
        this.value = value;
        this.line = line;
    }

    public Constraint(Class category, String value, int line) {
        this(category.getCanonicalName(), value, line);
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
}
