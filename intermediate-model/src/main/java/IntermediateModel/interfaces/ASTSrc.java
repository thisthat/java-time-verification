package IntermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTSrc {
	private static ASTSrc instance = null;
	public char[] source = {};

	protected ASTSrc() {
		// Exists only to defeat instantiation.
	}
	public static synchronized ASTSrc getInstance() {
		if(instance == null) {
			instance = new ASTSrc();
		}
		return instance;
	}
	public synchronized void setSource(char[] s){
		source = s;
	}

}
