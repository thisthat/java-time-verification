package test;

/**
 * Created by giovanni on 14/02/2017.
 */
public class Format {
    int start;
    int end;
    Type type;

    public enum Type {
        INSERT,
        DELETE,
        EDIT
    }

    public Format() {
    }

    public Format(int start, int end, Type type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
