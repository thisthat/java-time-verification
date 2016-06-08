package IntermediateModel.interfaces;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import java.util.Arrays;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public abstract class IASTStm {
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
	public enum Visibility {
		PUBLIC,
		PROTECT,
		ABSTRACT,
		FINAL,
		PRIVATE,
		STRICTFP
	}
	public int start;
	public int end;
	public Token startToken = null;
	public Token endToken = null;
	public String code = "";

	private boolean isTimeCritical = false;

	public boolean isTimeCritical(){
		return this.isTimeCritical;
	}

	public void setTimeCritical(boolean timeCritical) {
		isTimeCritical = timeCritical;
	}

	protected void calculateSourceCode(char[] source){
		code = Arrays.copyOfRange(source, start, end).toString();
	}

	protected void calculateSourceCode(){
		if(startToken == null || endToken == null){
			char[] source = ASTSrc.getInstance().source;
		}
		code = startToken.getInputStream().getText(new Interval(startToken.getStartIndex(), endToken.getStopIndex()));
		//code = new String(Arrays.copyOfRange(source, start, end));
	}

	public static String getSrcFromToken(Token start, Token end){
		return start.getInputStream().getText(new Interval(start.getStartIndex(), end.getStopIndex()));
	}

	protected String getCode(char[] source){
		if(code == null || code.equals("")){
			calculateSourceCode(source);
		}
		return code;
	}

	public int getLine(){
		return startToken.getLine();
	}

	protected IASTStm(Token start, Token end){
		this.start = start.getStartIndex();
		this.end = end.getStopIndex();
		this.startToken = start;
		this.endToken = end;
		calculateSourceCode();
	}

	protected IASTStm(int start, int end){
		this.start = start;
		this.end = end;
		calculateSourceCode();
	}
}
