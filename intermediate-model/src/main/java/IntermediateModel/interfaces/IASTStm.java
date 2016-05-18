package IntermediateModel.interfaces;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IASTStm {
	public enum Type {
		Break,
		Continue,
		For,
		Foreach,
		If,
		RE,
		Return,
		Switch,
		Try
	}
	public Token start;
	public Token end;
	public String code;

	protected void calculateSourceCode(){
		if(start == null || end == null){
			return;
		}
		if(start.getCharPositionInLine() < 0){
			return;
		}
		code = start.getInputStream().getText(new Interval(start.getStartIndex(), end.getStopIndex()));
	}

	protected IASTStm(Token start, Token end){
		this.start = start;
		this.end = end;
		calculateSourceCode();
	}
}
