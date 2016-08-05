package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.ASTREVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTUnary extends IASTStm implements IASTRE {

	private OPERATOR op;
	private IASTRE expr;

	public ASTUnary(Token start, Token end, OPERATOR op, IASTRE expr) {
		super(start, end);
		this.op = op;
		this.expr = expr;
	}

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
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTUnary(this);
		expr.visit(visitor);
	}
}
