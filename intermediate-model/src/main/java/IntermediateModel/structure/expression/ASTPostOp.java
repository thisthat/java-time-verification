package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTPostOp extends IASTStm implements IASTRE {

	private IASTRE var;
	private ADDDEC type;

	public ASTPostOp(Token start, Token end, IASTRE var, ADDDEC type) {
		super(start, end);
		this.var = var;
		this.type = type;
	}

	public ASTPostOp(int start, int end, IASTRE var, ADDDEC type) {
		super(start, end);
		this.var = var;
		this.type = type;
	}

	@Override
	public String toString() {
		return "ASTPostOp{" +
				"var=" + var +
				", type=" + type +
				'}';
	}
}
