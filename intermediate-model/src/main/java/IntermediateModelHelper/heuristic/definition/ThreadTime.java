package IntermediateModelHelper.heuristic.definition;

import IntermediateModelHelper.envirorment.BuildEnvironment;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTAttributeAccess;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;

/**
 * The following class implement a search in the ASTRE of Thread.sleep/Thread.join.
 * Thread classes can put the current execution to sleep with the sleep()/join() method.
 * This instruction models a time constraint. We can represent it in timed automata introducing a time constraint
 * between the transition from the sleep()/join() call to the next instruction.
 * The wait() instead is inherited from the Object class.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class ThreadTime extends SearchTimeConstraint {

	/**
	 * The search accept only {@link ASTRE}, in particular it checks only {@link ASTMethodCall}. <br>
	 * The search is focused in find the {@code Thread.sleep()}, {@code Thread.join()} and {@code object.wait()} calls.
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

		boolean found = false;

		String time = "wait";

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
					if( env.existVarName(var_name) && BuildEnvironment.getInstance().hasVarTypeTimeRelated(env.getVar(var_name))) //is a var of the env
					{
						found = true;
					}
				} else if(calee instanceof ASTAttributeAccess){
					//???
					ASTAttributeAccess attribute = (ASTAttributeAccess) calee;
					String className = "";
					if(attribute.getVariableName() instanceof ASTLiteral){
						className = ((ASTLiteral) attribute.getVariableName()).getValue();
					} else {
						//we have to use a failover technique
						className = attribute.getCode();
					}
					if(BuildEnvironment.getInstance().isTypeTimeRelated(className)){
						found = true;
					}
				}
			}

			if(found){
				if(mc.getParameters().size() > 0){
					if(mc.getParameters().get(0) instanceof ASTLiteral){
						time = ((ASTLiteral) mc.getParameters().get(0)).getValue();
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
					if( env.existVarName(var_name) && BuildEnvironment.getInstance().hasVarTypeTimeRelated(env.getVar(var_name))){
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
			this.addConstraint(time, stm);
		}

	}
}
