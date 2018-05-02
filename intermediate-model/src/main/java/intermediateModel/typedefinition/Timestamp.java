package intermediateModel.typedefinition;

public class Timestamp implements TimeType {
    @Override
    public String toString() {
        return "Timestamp";
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Timestamp;
    }
}
