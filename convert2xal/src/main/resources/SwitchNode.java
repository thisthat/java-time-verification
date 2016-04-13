/**
 *
 * This class is used to test if construct
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SwitchNode {
    private String dpid;

    @Override
    public boolean equalsElse(Object n){
        int i = 0;
        if(i > 0)
            return true;
        else
            i = 10;
        return false;
    }
}
