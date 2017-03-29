package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTBinary extends IASTStm implements IASTRE {

	protected IASTRE left;
	protected IASTRE right;
	protected OPERATOR op;

	public ASTBinary(int start, int end, IASTRE left, IASTRE right, OPERATOR op) {
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
		return "ASTBinary{" +
				"left=" + left +
				", right=" + right +
				", op=" + op +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterASTbinary(this);
		visitor.enterAll(this);
		left.visit(visitor);
		right.visit(visitor);
		visitor.exitASTbinary(this);
		visitor.exitAll(this);
	}

	@Override
	public String print() {
		return left.print() + op.print() + right.print();
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTbinary(this);
		left.visit(visitor);
		right.visit(visitor);
		visitor.exitASTbinary(this);
		visitor.exitAll(this);
	}
}
