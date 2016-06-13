package heuristic;


import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import IntermediateModel.visitors.DefualtASTREVisitor;
import envirorment.Env;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following class implement a search in the ASTRE of Thread.sleep/Thread.join
 *
 */
public class SocketTimeout extends SearchTimeConstraint {

	private String value_timeout = "";
	boolean found = false;

	@Override
	public void next(IASTStm stm, Env env) {
		if(!(stm instanceof ASTRE)) return;
		//works only on ASTRE
		IASTRE expr = ((ASTRE) stm).getExpression();
		//only search for Method Call
		final ASTMethodCall[] mc_search = {null};
		expr.visit(new DefualtASTREVisitor() {
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				mc_search[0] = elm;
			}
		});

		if(mc_search[0] == null && !(expr instanceof ASTMethodCall)) return;

		ASTMethodCall mc = mc_search[0] == null ? (ASTMethodCall) stm : mc_search[0];

		//Search for the timeout
		if(mc.getMethodName().equals("setSoTimeout")){
			if(mc.getExprCallee() instanceof ASTLiteral){
				String var_name = ((ASTLiteral) mc.getExprCallee()).getValue();
				if(!env.existVarName(var_name)) return;
				IASTRE par = mc.getParameters().get(0);
				if(par instanceof ASTLiteral){
					value_timeout = ((ASTLiteral) par).getValue();
				}
			}
		}
		String methodName = mc.getMethodName();
		if(methodName.equals("receive") || methodName.equals("getOutputStream")){
			if(mc.getExprCallee() instanceof ASTLiteral){
				String var_name = ((ASTLiteral) mc.getExprCallee()).getValue();
				if(!env.existVarName(var_name)) return;
			}
			found = true;
			stm.setTimeCritical(true);
		}
		if(found){
			this.addConstraint(stm.getLine(), stm.getCode());
			found = false;
		}

	}

	@Override
	public void next(IASTRE expr, Env env) {
		if(!(expr instanceof ASTMethodCall)) return;
		ASTMethodCall mc = (ASTMethodCall) expr;
		String methodName = mc.getMethodName();
		if(methodName.equals("receive") || methodName.equals("getOutputStream")){
			found = true;
			mc.setTimeCritical(true);
		}
		if(found){
			this.addConstraint(mc.getLine(), mc.getCode());
		}
	}
}
