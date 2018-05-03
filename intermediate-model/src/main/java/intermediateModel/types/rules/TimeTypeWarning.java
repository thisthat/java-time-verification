package intermediateModel.types.rules;

public class TimeTypeError extends TimeException {
    private String className;
    private String filename;
    private int line;
    private String cause;

    public TimeTypeError(String className, String filename, int line, String cause) {
        super(cause);
        this.className = className;
        this.filename = filename;
        this.line = line;
    }

    public TimeTypeError(int line, String cause) {
        super(cause);
        this.line = line;
    }

    public TimeTypeError(String className, String filename, TimeTypeError prev) {
        super(prev.getMessage());
        this.cause = prev.getMessage();
        this.line = prev.getLine();
        this.className = className;
        this.filename = filename;
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
