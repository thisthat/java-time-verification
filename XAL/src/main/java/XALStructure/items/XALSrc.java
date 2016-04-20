package XALStructure.items;

import XALStructure.XALItem;

/**
 * An extension of @see XALCLockVariable including the type od the variable and its default value.
 * Both are optional, the only one required is the name of the variable.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALSrc extends XALItem {



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
