package XALStructure.items;

/**
 * An extension of @see XALCLockVariable including the type od the variable and its default value.
 * Both are optional, the only one required is the name of the variable.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class XALVariable extends XALCLockVariable {

    public enum XALIO{
        I,
        O,
        IO
    }

    private String type;
    private String value = null;
    private XALIO IO = null;

    public XALVariable(String name) {
        super(name);
    }

    public XALVariable(String name, String type) {
        super(name);
        this.type = type;
    }

    public XALVariable(String name, String type, String value) {
        super(name);
        this.type = type;
        this.value = value;
    }

    public XALVariable(String name, String type, String value, XALIO io) {
        super(name);
        this.type = type;
        this.value = value;
        this.IO = io;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public XALIO getIO() {
        return IO;
    }

    public void setIO(XALIO IO) {
        this.IO = IO;
    }

    @Override
    public String toString(){
        String out = String.format("<Variable Name=\"%s\" ", this.name);
        if(this.type != null)
            out += String.format("Type=\"%s\" ", this.type);
        if(this.value != null)
            out += String.format("Value=\"%s\" ", this.value);
        if(this.IO != null)
            out += String.format("IO=\"%s\" ", this.IO);
        out += " />";
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
