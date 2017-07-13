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
		if(left != null)
			left.visit(visitor);
		if(right!= null)
			right.visit(visitor);
		visitor.exitASTbinary(this);
		visitor.exitAll(this);
	}

	@Override
	public String print() {
		return (left != null ? left.print() : "")
				+ op.print()
				+ (right != null ? right.print() : "");
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTbinary(this);
		if(left != null)
			left.visit(visitor);
		if(right!= null)
			right.visit(visitor);
		visitor.exitASTbinary(this);
		visitor.exitAll(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ASTBinary astBinary = (ASTBinary) o;

		if (left != null ? !left.equals(astBinary.left) : astBinary.left != null) return false;
		if (right != null ? !right.equals(astBinary.right) : astBinary.right != null) return false;
		return op == astBinary.op;
	}

	@Override
	public int hashCode() {
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		result = 31 * result + (op != null ? op.hashCode() : 0);
		return result;
	}
}
