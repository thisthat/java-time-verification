package intermediateModel.typedefinition;

public class Warning implements TimeType {
    @Override
    public String toString() {
        return "Warning";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Warning;
    }
}
