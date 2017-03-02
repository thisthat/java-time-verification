package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTPreOp extends IASTStm implements IASTRE {

	private IASTRE var;
	private ADDDEC type;

	public ASTPreOp(int start, int end, IASTRE var, ADDDEC type) {
		super(start, end);
		this.var = var;
		this.type = type;
	}

	@Override
	public String toString() {
		return "ASTPreOp{" +
				"var=" + var +
				", type=" + type +
				'}';
	}


	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTPreOp(this);
		var.visit(visitor);
		visitor.exitASTPreOp(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTPreOp(this);
		var.visit(visitor);
		visitor.exitASTPreOp(this);
		visitor.exitAll(this);
	}
}
