package XAL.items;

import XAL.XALItem;

/**
 * Class used to represent a time variable
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALCLockVariable extends XALItem {
    protected String name;

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public XALCLockVariable(String name) {
        this.name = name;
    }

    public String toString(int tab) {
        return String.format("<Variable Name=\"%s\" />", this.name);
    }

    /**
     * There are no constraint to validate
     * @return  Always true
     */
    protected boolean checkConstriant() {
        return true;
    }
}
