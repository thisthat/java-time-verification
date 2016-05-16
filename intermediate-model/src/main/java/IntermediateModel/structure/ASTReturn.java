package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTReturn extends IASTStm {

	private ASTRE expr;
	public ASTReturn(Token start, Token end, ASTRE expr) {
		super(start, end);
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "\t\t\treturn " + expr.toString();
	}
}
