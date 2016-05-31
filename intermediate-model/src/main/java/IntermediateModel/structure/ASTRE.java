package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTRE extends IASTStm {

	public ASTRE(Token start, Token end) {
		super(start.getStartIndex(), end.getStopIndex());
	}
	public ASTRE(int start, int end) {
		super(start, end);
	}

	@Override
	public String toString() {
		return "\t\t->" + code;
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
