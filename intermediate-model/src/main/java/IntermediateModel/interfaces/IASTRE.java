package intermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTRE extends IASTVisitor {
	enum OPERATOR {
		less,			// <
		lessEqual,		// <=
		notEqual,		// !=
		equal,			// =
		equality,		// ==
		greater,		// >
		greaterEqual,	// >=
		shiftLeft,		// <<
		shiftRight,		// >>
		plus,			// +
		minus,			// -
		mul,			// *
		div,			// /
		not,			// !
		mod,			// %
		and,			// &&
		or,				// ||
		instanceOf		// instanceOf
		;


		@Override
		public String toString() {
			if(this.equals(OPERATOR.equality))
				return "equal";
			return super.toString();
		}
	}

	enum ADDDEC {
		increment,
		decrement
	}

	void visit(ASTREVisitor visitor);
	String getCode();
	int getLine();

}
