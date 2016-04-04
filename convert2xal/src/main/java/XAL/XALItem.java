package XAL;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public abstract class XALItem {

    protected String tab(int n){
        if(n == 0)
            return "";
        else
            return "\t" + tab(n-1);
    }

}
