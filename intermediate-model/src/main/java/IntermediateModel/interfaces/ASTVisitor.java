package intermediateModel.interfaces;


import intermediateModel.structure.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface ASTVisitor<T> extends ASTREVisitor {
	void enterASTAttribute(ASTAttribute elm);
	void enterASTBreak(ASTBreak elm);
	void enterASTClass(ASTClass elm);
	void enterASTConstructor(ASTConstructor elm);
	void enterASTContinue(ASTContinue elm);
	void enterASTDoWhile(ASTDoWhile elm);
	void enterASTFor(ASTFor elm);
	void enterASTForEach(ASTForEach elm);
	void enterASTIf(ASTIf elm);
	void enterASTImport(ASTImport elm);
	void enterASTMethod(ASTMethod elm);
	void enterASTRE(ASTRE elm);
	void enterASTReturn(ASTReturn elm);
	void enterASTStatic(ASTStatic elm);
	void enterASTSwitch(ASTSwitch elm);
	void enterASTSynchronized(ASTSynchronized elm);
	void enterASTThrow(ASTThrow elm);
	void enterASTTry(ASTTry elm);
	void enterASTTryResources(ASTTryResources elm);
	void enterASTVariable(ASTVariable elm);
	void enterASTWhile(ASTWhile elm);
	void enterASTHiddenClass(ASTHiddenClass astHiddenClass);
}
