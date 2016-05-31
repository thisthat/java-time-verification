package IntermediateModel.interfaces;

import java.util.Arrays;

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
	public int start;
	public int end;
	public String code = "";

	protected void calculateSourceCode(char[] source){
		code = Arrays.copyOfRange(source, start, end).toString();
	}

	protected void calculateSourceCode(){
		char[] source = ASTSrc.getInstance().source;
		code = new String(Arrays.copyOfRange(source, start, end+1));
	}

	protected String getCode(char[] source){
		if(code == null || code.equals("")){
			calculateSourceCode(source);
		}
		return code;
	}


	protected IASTStm(int start, int end){
		this.start = start;
		this.end = end;
		calculateSourceCode();
	}
}
