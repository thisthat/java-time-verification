package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTLiteral extends IASTStm implements IASTRE {
	private String value;

	public ASTLiteral(int start, int end, String value) {
		super(start, end);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ASTLiteral{" +
				"value='" + value + '\'' +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTLiteral(this);
		visitor.exitASTLiteral(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTLiteral(this);
		visitor.exitASTLiteral(this);
		visitor.exitAll(this);
	}
}
