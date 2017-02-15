package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTConditional extends IASTStm implements IASTRE {

	IASTRE expr;
	IASTRE thenExpr;
	IASTRE elseExpr;

	public ASTConditional(int start, int end, IASTRE expr, IASTRE thenExpr, IASTRE elseExpr) {
		super(start, end);
		this.expr = expr;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
	}

	public IASTRE getExpr() {
		return expr;
	}

	public IASTRE getThenExpr() {
		return thenExpr;
	}

	public IASTRE getElseExpr() {
		return elseExpr;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTConditional(this);
		expr.visit(visitor);
		thenExpr.visit(visitor);
		elseExpr.visit(visitor);
		visitor.exitASTConditional(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTConditional(this);
		expr.visit(visitor);
		thenExpr.visit(visitor);
		elseExpr.visit(visitor);
		visitor.exitASTConditional(this);
		visitor.exitAll(this);
	}
}
