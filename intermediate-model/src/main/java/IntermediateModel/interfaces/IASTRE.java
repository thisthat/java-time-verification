package IntermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTRE {
	public enum OPERATOR {
		less,
		lessEqual,
		equal,
		equality,
		greater,
		greaterEqual,
		shiftLeft,
		shiftRight,
		plus,
		minus,
		mul,
		div,
		not
	}

	public enum ADDDEC {
		increment,
		decrement
	}

	public void visit(ASTREVisitor visitor);
}
