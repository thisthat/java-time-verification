package intermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTRE extends IASTVisitor {
	enum OPERATOR {
		less,			// <
		lessEqual,		// <=
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
	}

	enum ADDDEC {
		increment,
		decrement
	}

	void visit(ASTREVisitor visitor);

}
