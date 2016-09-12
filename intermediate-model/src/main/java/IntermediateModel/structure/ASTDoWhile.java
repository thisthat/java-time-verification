package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTDoWhile extends ASTWhile implements IASTVisitor {


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


	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTDoWhile(this);
		expr.visit(visitor);
		for(IASTStm s : stms){
			s.visit(visitor);
		}
		visitor.exitASTDoWhile(this);
	}
}
