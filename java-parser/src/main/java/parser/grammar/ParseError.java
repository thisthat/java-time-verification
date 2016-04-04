package parser.grammar;

/**
 * Class to store parse errors
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ParseError {
    private int lastErrorLine;
    private int lastErrorChar;
    private String lastErrorMsg;

    public ParseError(int lastErrorLine, int lastErrorChar, String lastErrorMsg) {
        this.lastErrorLine = lastErrorLine;
        this.lastErrorChar = lastErrorChar;
        this.lastErrorMsg = lastErrorMsg;
    }

    public int getLastErrorLine() {
        return lastErrorLine;
    }

    public void setLastErrorLine(int lastErrorLine) {
        this.lastErrorLine = lastErrorLine;
    }

    public int getLastErrorChar() {
        return lastErrorChar;
    }

    public void setLastErrorChar(int lastErrorChar) {
        this.lastErrorChar = lastErrorChar;
    }

    public String getLastErrorMsg() {
        return lastErrorMsg;
    }

    public void setLastErrorMsg(String lastErrorMsg) {
        this.lastErrorMsg = lastErrorMsg;
    }
}
