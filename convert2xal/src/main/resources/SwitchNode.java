/**
 *
 * This class is used to test if construct
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SwitchNode {
    private String dpid;

    @Override
    public boolean equals(Object n){
        if(n.getClass().equals(this.getClass()))
            return ((SwitchNode) n).getName().equals(this.dpid);
        return false;
    }

    @Override
    public boolean equalsElse(Object n){
        if(n.getClass().equals(this.getClass()))
            return ((SwitchNode) n).getName().equals(this.dpid);
        else
            return false;
    }
}
