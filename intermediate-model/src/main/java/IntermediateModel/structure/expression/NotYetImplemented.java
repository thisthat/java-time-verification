package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class NotYetImplemented extends IASTStm implements IASTRE {

	StackTraceElement[] cause = Thread.currentThread().getStackTrace();
	String typeElm = null;

	public NotYetImplemented(Token start, Token end) {
		super(start, end);
	}

	public NotYetImplemented(int start, int end) {
		super(start, end);
	}

	public NotYetImplemented(Token start, Token end, String typeElm) {
		super(start, end);
		this.typeElm = typeElm;
	}

	public NotYetImplemented(int start, int end, String typeElm) {
		super(start, end);
		this.typeElm = typeElm;
	}

	public String toString(){
		return "Not Yet Implemented :: " + code + " @ " + cause[2] + ((typeElm != null) ? "--" + typeElm : "");
	}
}