package IntermediateModelHelper.heuristic.definition;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;

import java.util.List;

/**
 *
 * All the statements that use objects from java.util.Timer represent a time constraint.
 * The semantic of the timer is to execute a callback when the amount of time specified in the constructor expired.
 * This is a particular case of the previous heuristic.
 * A suggestion how to model this behaviour in terms of timed automaton is to create a state that represent the
 * execution of the callback.
 * From each state, that follows the creation of the timer in the current block of code, add a new transition
 * to the callback state with the temporal constraint.
 * These new transitions do not alter the normal execution flow,
 * they will spawn a thread that will execute the callback.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class TimerType extends SearchTimeConstraint {

	/**
	 * We check all the calls to the {@code schedule()} method. If the variable that execute the method
	 * is in the envirorment and it has type <b>Timer</b>, then we add the instruction to the list of time constraint.
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
		//get all the vars with Timer type
		List<IASTVar> var_list = env.getVarsByType("Timer");
		ASTMethodCall mc = (ASTMethodCall) expr;

		if(mc.getMethodName().equals("schedule")){ //we call schedule
			if(mc.getExprCallee() instanceof ASTLiteral){ //on a var
				String vName = ((ASTLiteral) mc.getExprCallee()).getValue();
				if(var_list.stream().anyMatch( name -> name.getName().equals(vName))){ //that is in the env with type Timer
					stm.setTimeCritical(true);

					//get value of delay
					if(mc.getParameters().size() > 1){
						IASTRE r = mc.getParameters().get(1);
						if(r instanceof ASTLiteral){
							//stm.addConstraint(stm.getLine(), ((ASTLiteral) r).getValue(), TimerType.class);
							this.addConstraint(((ASTLiteral) r).getValue(), stm);
						} else {
							stm.addConstraint(stm.getLine(), r.toString(), TimerType.class);
							//this.addConstraint(((ASTLiteral) r).getValue(), stm);
						}
					}
				}
			}
		}
	}

}
