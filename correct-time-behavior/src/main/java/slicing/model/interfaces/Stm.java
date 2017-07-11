package slicing.model;

import slicing.model.interfaces.IReducedVisitor;

/**
 * Created by giovanni on 11/07/2017.
 */
public abstract class Stm implements IReducedVisitor {


    protected int start;
    protected int end;
    protected int line;
    protected int lineEnd;

    protected String code = "";

    public Stm(int start, int end, int line, int lineEnd, String code) {
        this.start = start;
        this.end = end;
        this.line = line;
        this.lineEnd = lineEnd;
        this.code = code;
    }


    public String getCode(){
        return code;
    }

    /**
     * Get the line in the file of the node
     * @return the line number
     */
    public int getLine(){
        return line;
    }

    /**
     * Get the end line in the file of the node
     * @return the end line number
     */
    public int getLineEnd(){
        return lineEnd;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
