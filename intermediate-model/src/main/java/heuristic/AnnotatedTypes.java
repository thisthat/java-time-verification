package heuristic;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import IntermediateModel.structure.expression.ASTMultipleMethodCall;
import envirorment.Env;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnnotatedTypes extends SearchTimeConstraint {

	@Override
	public void next(IASTStm stm, Env env) {
		if(!(stm instanceof ASTRE)) return;
		//works only on ASTRE
		IASTRE expr = ((ASTRE) stm).getExpression();
		//only search for Method Call
		if(!(expr instanceof ASTMethodCall)) return;
		ASTMethodCall method = (ASTMethodCall) expr;

		//TODO extends this search with the full typesystem of the project :: for each method its return type

		boolean found = false;

		String methodName = method.getMethodName();
		IASTRE calee = method.getExprCallee();
		if(calee instanceof ASTLiteral){
			ASTLiteral literalName = (ASTLiteral) calee;
			String varName = literalName.getValue();
			//check in the env
			if(env.existVarName(varName) && env.existMethod(methodName)){
				stm.setTimeCritical(true);
				found = true;
			}
		}
		if(calee instanceof ASTMultipleMethodCall){
			ASTMultipleMethodCall mCalee = (ASTMultipleMethodCall) calee;
			String varName = ((ASTLiteral) mCalee.getVariable()).getValue();
			//at least one in the chain is time
			for(IASTRE e : mCalee.getMethods()){
				ASTMethodCall m = (ASTMethodCall) e;
				String mName = m.getMethodName();
				if(env.existVarName(varName) && env.existMethod(mName)){
					stm.setTimeCritical(true);
					found = true;
				}
			}
		}

		if(found){
			this.addConstraint(stm);
		}

	}

	@Override
	public void next(IASTRE expr, Env env) {

	}

}
