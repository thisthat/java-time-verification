package heuristic;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
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

		if(!(expr instanceof ASTMethodCall)) return;
		//only search for Method Call
		ASTMethodCall mc = (ASTMethodCall) expr;
		if(mc.getMethodName().equals("sleep")){
			if(mc.getExprCallee() instanceof ASTLiteral && ((ASTLiteral) mc.getExprCallee()).getValue().equals("Thread")){
				System.err.println("Found @" + stm.getLine());
			}
		}

	}
}
