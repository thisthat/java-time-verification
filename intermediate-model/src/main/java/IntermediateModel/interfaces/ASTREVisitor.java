package IntermediateModel.interfaces;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.ASTAttribute;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTVariable;
import IntermediateModel.structure.expression.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface ASTREVisitor<T> {
	void enterASTAssignment(ASTAssignment elm);
	void enterASTAttributeAccess(ASTAttributeAccess elm);
	void enterASTbinary(ASTBinary elm);
	void enterASTCast(ASTCast elm);
	void enterASTLiteral(ASTLiteral elm);
	void enterASTMethodCall(ASTMethodCall elm);
	void enterASTMultipleMethodCall(ASTMultipleMethodCall elm);
	void enterASTNewObject(ASTNewObject elm);
	void enterASTPostOp(ASTPostOp elm);
	void enterASTPreOp(ASTPreOp elm);
	void enterASTUnary(ASTUnary elm);
	void enterASTVariableDeclaration(ASTVariableDeclaration elm);
	void enterElse(NotYetImplemented elm);
}
