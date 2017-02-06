package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTAttributeAccess extends IASTStm implements IASTRE {

	private String attributeName;
	private IASTRE variableName;


	public ASTAttributeAccess(int start, int end, IASTRE variableName, String attributeName) {
		super(start, end);
		this.variableName = variableName;
		this.attributeName = attributeName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public IASTRE getVariableName() {
		return variableName;
	}

	@Override
	public String toString() {
		return "ASTAttributeAccess{" +
				"variableName='" + variableName + "\'," +
				"attributeName='" + attributeName + '\'' +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTAttributeAccess(this);
		visitor.exitASTAttributeAccess(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTAttributeAccess(this);
		visitor.exitASTAttributeAccess(this);
		visitor.exitAll(this);
	}
}
