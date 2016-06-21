package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTContinue extends IASTStm implements IASTVisitor {

	public ASTContinue(Token start, Token end) {
		super(start, end);
	}
	public ASTContinue(int start, int end) {
		super(start, end);
	}
	@Override
	public String toString() {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTContinue(this);
	}
}
