package intermediateModel.types.rules.exception;

import java.util.Objects;

public class TimeTypeRecommendation extends TimeException {
    private String className;
    private String filename;
    private int line;
    private String cause;

    public TimeTypeRecommendation(String className, String filename, int line, String cause) {
        super(cause);
        this.className = className;
        this.filename = filename;
        this.line = line;
    }

    public TimeTypeRecommendation(int line, String cause) {
        super(cause);
        this.line = line;
    }

    public TimeTypeRecommendation(String className, String filename, TimeTypeRecommendation prev) {
        super(prev.getMessage());
        this.cause = prev.getMessage();
        this.line = prev.getLine();
        this.className = className;
        this.filename = filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeTypeRecommendation)) return false;
        TimeTypeRecommendation that = (TimeTypeRecommendation) o;
        return getLine() == that.getLine() &&
                Objects.equals(getClassName(), that.getClassName()) &&
                Objects.equals(getFilename(), that.getFilename());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getClassName(), getFilename(), getLine(), getCause());
    }

    public String getClassName() {
        return className;
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
        return line;
    }


    public String getCauseMsg() {
        return cause;
    }

    public String getFullMessage() {
        return String.format(
                "%s - %s -- [%d] %s",
                filename, className, line, cause
        );
    }
}
