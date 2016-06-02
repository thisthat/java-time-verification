package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTPostInc extends IASTStm implements IASTRE {
	private IASTRE var;

	public ASTPostInc(Token start, Token end, IASTRE var) {
		super(start, end);
		this.var = var;
	}

	public ASTPostInc(int start, int end, IASTRE var) {
		super(start, end);
		this.var = var;
	}
}
