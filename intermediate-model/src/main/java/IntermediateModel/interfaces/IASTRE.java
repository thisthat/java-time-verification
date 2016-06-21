package intermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTRE extends IASTVisitor {
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
		not,
		and,
		or,
		instanceOf
	}

	public enum ADDDEC {
		increment,
		decrement
	}

	public void visit(ASTREVisitor visitor);
}
