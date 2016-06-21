package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.ASTREVisitor;
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
		return "Not Yet Implemented :: [" + start +"]" + code + " @ " + getLine() + "--" + cause[2] + ((typeElm != null) ? "--" + typeElm : "");
	}


	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterNotYetImplemented(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterNotYetImplemented(this);
	}

}
