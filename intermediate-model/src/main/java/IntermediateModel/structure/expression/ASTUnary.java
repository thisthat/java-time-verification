package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTUnary extends IASTStm implements IASTRE {

	private OPERATOR op;
	private IASTRE expr;

	public ASTUnary(int start, int end, OPERATOR op, IASTRE expr) {
		super(start, end);
		this.op = op;
		this.expr = expr;
	}

	public OPERATOR getOp() {
		return op;
	}

	public IASTRE getExpr() {
		return expr;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTUnary(this);
		expr.visit(visitor);
		visitor.exitASTUnary(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTUnary(this);
		expr.visit(visitor);
		visitor.exitASTUnary(this);
		visitor.exitAll(this);
	}
}
