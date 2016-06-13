package heuristic;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import envirorment.Env;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following class search for Timer Objects
 *
 */
public class TimerType extends SearchTimeConstraint {

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
					this.addConstraint(stm);
				}
			}

		}
	}

	@Override
	public void next(IASTRE expr, Env env) {

	}
}
