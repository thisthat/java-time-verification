package intermediateModel.interfaces;


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.javatuples.Triplet;
import parser.ASTSrc;

import java.util.Arrays;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public abstract class IASTStm implements IASTVisitor{
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
	public int line;
	protected Triplet<Integer,String, Class> constraint = null;

	public Triplet<Integer,String, Class> getConstraint(){
		return constraint;
	}

	public void addConstraint(Integer line, String msg, Class heuristic){
		isTimeCritical = true;
		constraint = new Triplet<>(line,msg, heuristic);
	}

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
			ASTSrc instance = ASTSrc.getInstance();
			char[] source = instance.source;
			line = instance.getLine(start) +1;
			code = new String(Arrays.copyOfRange(source, start, end));
			return;
		}
		code = startToken.getInputStream().getText(new Interval(startToken.getStartIndex(), endToken.getStopIndex()));
		line = startToken.getLine();
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
	public String getCode(){
		if(code == null || code.equals("")){
			calculateSourceCode();
		}
		return code;
	}

	public int getLine(){
		return line;
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
