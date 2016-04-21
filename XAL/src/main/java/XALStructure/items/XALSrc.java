package XALStructure.items;

import XALStructure.XALItem;
import org.antlr.v4.runtime.CommonToken;

/**
 * <b>!!!!Experimental!!!!</b>
 * This Element is used to store src code.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALSrc extends XALItem {

    CommonToken start;
    CommonToken stop;

    /**
     * Get the Starting Token
     * @return  CommonToken structure of Antlr4
     */
    public CommonToken getStart() {
        return start;
    }

    /**
     * Set the start Token
     * @param start
     */
    public void setStart(CommonToken start) {
        this.start = start;
    }

    /**
     * Get the Stop Token
     * @return  CommonToken structure of Antlr4
     */
    public CommonToken getStop() {
        return stop;
    }

    /**
     * Set the stop Token
     * @param stop
     */
    public void setStop(CommonToken stop) {
        this.stop = stop;
    }

    /**
     * Constructor of the class. The two parameters are, respectively, the start and stop tokens.
     * @param start
     * @param stop
     */
    public XALSrc(CommonToken start, CommonToken stop) {
        this.start = start;
        this.stop = stop;
    }

    public XALSrc() { }

    @Override
    public String toString(){
        String out = "";
        return out;
    }

    @Override
    public String toString(int tab) {
        return tab(tab) + this.toString();
    }

    /**
     * No constraint to check.
     * @return Always true.
     */
    @Override
    protected boolean checkConstriant() {
        return true;
    }
}
