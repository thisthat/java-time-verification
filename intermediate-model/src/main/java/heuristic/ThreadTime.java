package heuristic;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTAttributeAccess;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import envirorment.BuildEnvirormentClass;
import envirorment.Env;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following class implement a search in the ASTRE of Thread.sleep/Thread.join
 *
 */
public class ThreadTime extends SearchTimeConstraint {

	@Override
	public void next(IASTStm stm, Env env) {
		if(!(stm instanceof ASTRE)) return;
		//works only on ASTRE
		IASTRE expr = ((ASTRE) stm).getExpression();
		//only search for Method Call
		if(!(expr instanceof ASTMethodCall)) return;

		boolean found = false;

		//Search if is in the form Thread.sleep
		ASTMethodCall mc = (ASTMethodCall) expr;
		if(mc.getMethodName().equals("sleep")){
			if(mc.getExprCallee() instanceof ASTLiteral && ((ASTLiteral) mc.getExprCallee()).getValue().equals("Thread")){
				found = true;
			}
			else { //Search if is it in the form var.sleep
				IASTRE calee = mc.getExprCallee();
				if( calee instanceof ASTLiteral){
					String var_name = ((ASTLiteral) mc.getExprCallee()).getValue();
					if( env.existVarName(var_name) || //is a var of the env
						BuildEnvirormentClass.typeTimeRelevant.stream().anyMatch(type -> (var_name.equals(type))) //static var
					){
						found = true;
					}
				} else if(calee instanceof ASTAttributeAccess){
					String callReconstructed = ((ASTAttributeAccess) calee).getCode();
					if( BuildEnvirormentClass.typeTimeRelevant.stream().anyMatch(type -> (callReconstructed.equals(type))) //static var
							){
						found = true;
					}
				}
			}
		}
		//search for join
		if(mc.getMethodName().equals("join")){
			//in form Thread.join
			if(mc.getExprCallee() instanceof ASTLiteral && ((ASTLiteral) mc.getExprCallee()).getValue().equals("Thread")){
				found = true;
			} else {
				//in form var.join()
				if(mc.getExprCallee() instanceof ASTLiteral){
					String var_name = ((ASTLiteral) mc.getExprCallee()).getValue();
					if( env.existVarName(var_name) || //is a var of the env
						BuildEnvirormentClass.typeTimeRelevant.stream().anyMatch(type -> (var_name.equals(type))) //static var
					){
						found = true;
					}
				}
			}
		}
		//search for wait
		if(mc.getMethodName().equals("wait")){
			found = true;
		}
		if(found){
			this.addConstraint(stm);
		}

		//System.out.println(mc.toString());
	}

	@Override
	public void next(IASTRE expr, Env env) {

	}
}
