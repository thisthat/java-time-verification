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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTReturn)) return false;

		ASTReturn astReturn = (ASTReturn) o;

		if (expr != null ? !expr.equals(astReturn.expr) : astReturn.expr != null) return false;

		return true;
	}

}