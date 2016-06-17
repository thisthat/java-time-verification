package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.*;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTAttributeAccess extends IASTStm implements IASTRE {

	private String attributeName;
	private IASTRE variableName;

	public ASTAttributeAccess(Token start, Token end, IASTRE variableName, String attributeName) {
		super(start, end);
		this.variableName = variableName;
		this.attributeName = attributeName;
	}

	public ASTAttributeAccess(int start, int end, IASTRE variableName, String attributeName) {
		super(start, end);
		this.variableName = variableName;
		this.attributeName = attributeName;
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
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTAttributeAccess(this);
	}
}
