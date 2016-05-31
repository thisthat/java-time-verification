package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTThrow extends IASTStm {

	private ASTRE expr;
	public ASTThrow(Token start, Token end, ASTRE expr) {
		super(start.getStartIndex(), end.getStopIndex());
		this.expr = expr;
	}

	public ASTThrow(int start, int end, ASTRE expr) {
		super(start, end);
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "\t\t\tthrow " + expr.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTThrow)) return false;

		ASTThrow astReturn = (ASTThrow) o;

		if (expr != null ? !expr.equals(astReturn.expr) : astReturn.expr != null) return false;

		return true;
	}

}
