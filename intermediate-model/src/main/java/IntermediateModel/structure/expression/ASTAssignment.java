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
public class ASTAssignment extends IASTStm implements IASTRE {

	protected IASTRE left;
	protected IASTRE right;
	protected OPERATOR op;

	public ASTAssignment(Token start, Token end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end);
		this.left = left;
		this.right = right;
		this.op = op;
	}

	public ASTAssignment(int start, int end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end);
		this.left = left;
		this.right = right;
		this.op = op;
	}

	public IASTRE getLeft() {
		return left;
	}

	public void setLeft(IASTRE left) {
		this.left = left;
	}

	public IASTRE getRight() {
		return right;
	}

	public void setRight(IASTRE right) {
		this.right = right;
	}

	public OPERATOR getOp() {
		return op;
	}

	public void setOp(OPERATOR op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return "ASTAssignment{" +
				"left=" + left +
				", right=" + right +
				", op=" + op +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTAssignment(this);
		left.visit(visitor);
		right.visit(visitor);
		visitor.exitASTAssignment(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTAssignment(this);
		left.visit(visitor);
		right.visit(visitor);
		visitor.exitASTAssignment(this);
		visitor.exitAll(this);
	}
}
