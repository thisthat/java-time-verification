package IntermediateModelHelper.heuristic;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.structure.expression.ASTMultipleMethodCall;
import IntermediateModelHelper.envirorment.Env;


/**
 *
 * TODO: Define how process user annotations.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnnotatedTypes extends SearchTimeConstraint {

	/**
	 * The search accept only {@link ASTRE}, in particular it checks only {@link ASTMethodCall}. <br>
	 * The heuristc search if the method called is in the list of time relevant methods.
	 * Moreover, in order to be a time method, the calee expression must be in the envirorment as well.
	 *
	 * @param stm	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
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
			if(env.existVarNameTimeRelevant(varName) && env.existMethodTimeRelevant(methodName)){
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
				if(env.existVarNameTimeRelevant(varName) && env.existMethodTimeRelevant(mName)){
					stm.setTimeCritical(true);
					found = true;
				}
			}
		}
		if(found){
			this.addConstraint(stm);
		}
	}

}
