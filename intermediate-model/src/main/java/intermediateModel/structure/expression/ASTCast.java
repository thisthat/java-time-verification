package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTCast extends IASTStm implements IASTRE {

	private String type;
	private IASTRE expr;

	public ASTCast(int start, int end, String type, IASTRE expr) {
		super(start, end);
		this.type = type;
		this.expr = expr;
	}

	public String getType() {
		return type;
	}

	public IASTRE getExpr() {
		return expr;
	}

	@Override
	public String toString() {
		return "ASTCast{" +
				"type='" + type + '\'' +
				", expr=" + expr +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTCast(this);
		expr.visit(visitor);
		visitor.exitASTCast(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTCast(this);
		expr.visit(visitor);
		visitor.exitASTCast(this);
		visitor.exitAll(this);
	}


}
