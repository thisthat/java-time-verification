package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTDoWhile extends ASTWhile {


	public ASTDoWhile(Token start, Token end) {
		super(start, end);
	}

	public ASTDoWhile(Token start, Token end, ASTRE expr) {
		super(start, end, expr);
	}

	public ASTDoWhile(int start, int end) {
		super(start, end);
	}

	public ASTDoWhile(int start, int end, ASTRE expr) {
		super(start, end, expr);
	}

	@Override
	public String toString() {
		String out = "";
		out += expr.toString();
		out += "\n";
		for(IASTStm e : stms){
			out += e.toString() + "\n";
		}
		out += "";
		return out;
	}

}
