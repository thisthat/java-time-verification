package intermediateModel.visitors;

import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public abstract class ConvertIM {

	protected void dispachStm(List<IASTStm> stms){
		for(IASTStm stm : stms){
			dispachStm(stm);
		}
	}
	/**
	 * Ugly workaround to have pattern matching in haskel fashion on java
	 * @param stm	Statement to analyze
	 */
	protected void dispachStm(IASTStm stm){
		if (stm instanceof ASTRE){
			convertRE((ASTRE) stm);
		}
		else if(stm	instanceof ASTBreak){
			convertBreak((ASTBreak)stm);
		}
		else if(stm	instanceof ASTContinue){
			convertContinue((ASTContinue) stm);
		}
		else if(stm	instanceof ASTDoWhile){
			convertDoWhile((ASTDoWhile)stm);
		}
		else if(stm	instanceof ASTFor){
			convertFor((ASTFor)stm);
		}
		else if(stm	instanceof ASTForEach){
			convertForeach((ASTForEach)stm);
		}
		else if(stm	instanceof ASTIf){
			convertIf((ASTIf)stm);
		}
		else if(stm	instanceof ASTReturn){
			convertReturn((ASTReturn)stm);
		}
		else if(stm	instanceof ASTSwitch){
			convertASTSwitch((ASTSwitch)stm);
		}
		else if(stm	instanceof ASTSynchronized){
			convertSynchronized((ASTSynchronized)stm);
		}
		else if(stm	instanceof ASTThrow){
			convertThrow((ASTThrow)stm);
		}
		//the order is important because ASTTry is extended by ASTTryResources
		else if(stm	instanceof ASTTryResources){
			convertTryResource((ASTTryResources)stm);
		}
		else if(stm	instanceof ASTTry){
			convertTry((ASTTry)stm);
		}
		else if(stm	instanceof ASTWhile){
			convertWhile((ASTWhile)stm);
		}
		else if(stm instanceof ASTNewObject){
			convertASTNewObject((ASTNewObject) stm);
		}
		else if(stm instanceof ASTHiddenClass){
			convertASTHiddenClass((ASTHiddenClass)stm);
		}
	}




	protected void convertASTHiddenClass(ASTHiddenClass stm) {

	}
	protected void convertASTNewObject(ASTNewObject stm) {

	}
	protected void convertWhile(ASTWhile stm) {

	}

	protected void convertTry(ASTTry stm) {

	}

	protected void convertTryResource(ASTTryResources stm) {

	}

	protected void convertThrow(ASTThrow stm) {

	}

	protected void convertSynchronized(ASTSynchronized stm) {

	}

	protected void convertASTSwitch(ASTSwitch stm) {

	}

	protected void convertReturn(ASTReturn stm) {

	}

	protected void convertIf(ASTIf stm) {
	}

	protected void convertForeach(ASTForEach stm) {
	}

	protected void convertFor(ASTFor stm) {

	}

	protected void convertDoWhile(ASTDoWhile stm) {
	}

	protected void convertContinue(ASTContinue stm) {
	}

	protected void convertBreak(ASTBreak stm){}

	protected void convertRE(ASTRE stm){}
}
