package intermediateModel.typedefinition;

import com.fasterxml.jackson.annotation.JsonValue;

public class Warning implements TimeType {
    @JsonValue
    @Override
    public String toString() {
        return "Warning";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Warning;
    }
}
