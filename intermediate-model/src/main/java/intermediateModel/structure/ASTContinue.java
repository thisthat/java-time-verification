package IntermediateModel.structure;

import IntermediateModel.interfaces.ASTVisitor;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVisitor;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTContinue extends IASTStm implements IASTVisitor {

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
		visitor.exitASTContinue(this);
	}
}
