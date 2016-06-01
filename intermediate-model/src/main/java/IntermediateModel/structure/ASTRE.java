package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTRE extends IASTStm {

	IASTRE expression;

	public ASTRE(Token start, Token end, IASTRE expression) {
		super(start, end);
		this.expression = expression;
	}
	public ASTRE(int start, int end, IASTRE expression) {
		super(start, end);
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "\t\t->" + expression.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
