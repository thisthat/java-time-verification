package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTBreak extends IASTStm implements IASTVisitor {

	public ASTBreak(Token start, Token end) {
		super(start, end);
	}
	public ASTBreak(int start, int end){ super(start,end);}

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
		visitor.enterASTBreak(this);
	}
}
