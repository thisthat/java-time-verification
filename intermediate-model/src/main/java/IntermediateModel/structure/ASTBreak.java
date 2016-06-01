package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTBreak extends IASTStm {

	public ASTBreak(Token start, Token end) {
		super(start, end);
	}
	public ASTBreak(int start, int end){ super(start,end);}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
