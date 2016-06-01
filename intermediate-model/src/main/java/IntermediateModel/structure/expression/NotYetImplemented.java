package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class NotYetImplemented extends IASTStm implements IASTRE {

	public NotYetImplemented(Token start, Token end) {
		super(start, end);
	}

	public NotYetImplemented(int start, int end) {
		super(start, end);
	}

	public String toString(){
		return "Not Yet Implemented :: " + code;
	}
}
