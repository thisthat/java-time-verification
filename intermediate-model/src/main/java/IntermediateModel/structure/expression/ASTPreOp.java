package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.ASTREVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTPreOp extends IASTStm implements IASTRE {

	private IASTRE var;
	private ADDDEC type;

	public ASTPreOp(Token start, Token end, IASTRE var, ADDDEC type) {
		super(start, end);
		this.var = var;
		this.type = type;
	}

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
		visitor.enterASTPreOp(this);
		var.visit(visitor);
	}
}
