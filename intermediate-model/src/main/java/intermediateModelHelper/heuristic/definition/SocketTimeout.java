package intermediateModelHelper.heuristic.definition;


import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;

/**
 *
 * The class implement a search for the setSoTime out.
 * In socket programming the use of timeout is necessary to not wait endless messages that will be never dispatches.
 * An approach is to model these time constraint has a particular case of sleep.
 * The difference with the sleep is that the time constraint has not to be satisfied in the current state,
 * but it will be deferred to the <i>“receive”/"getOutputStream"</i> method call.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
@Deprecated
public class SocketTimeout extends SearchTimeConstraint {

	private String value_timeout = "";
	boolean found = false;
	private Integer __SOCKET_SOTIMEOUT = 0;


	@Override
	public void setup(ASTClass c) {

	}

	/**
	 * The search accept only {@link ASTRE}, in particular it checks only {@link ASTMethodCall}. <br>
	 * It collects the {@link ASTMethodCall} in the RExp and search for the definition of the method <b>setSoTimeout</b>
	 * from which it extracts the time value. Then when it finds the call to <b>receive</b> or <b>getOutputStream</b>
	 * it saves the time constraint (if the variable that calls the methods is time relevant as well).
	 *
	 * @param stm	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
	@Override
	public void next(ASTRE stm, Env env) {

		//works only on ASTRE
		IASTRE expr = stm.getExpression();
		//only search for Method Call
		final ASTMethodCall[] mc_search = {null};
		expr.visit(new DefualtASTREVisitor() {
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				mc_search[0] = elm;
			}
		});

		if(mc_search[0] == null && !(expr instanceof ASTMethodCall)) return;

		//ASTMethodCall mc = mc_search[0] == null ? (ASTMethodCall) stm : mc_search[0];
		ASTMethodCall mc = mc_search[0];

		//Search for the timeout
		if(mc.getMethodName().equals("setSoTimeout")){
			if(mc.getExprCallee() instanceof ASTLiteral){
				String var_name = ((ASTLiteral) mc.getExprCallee()).getValue();
				if(!env.existVarName(var_name)) return;
				env.addFlag(var_name, __SOCKET_SOTIMEOUT);
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
				if(env.existVarName(var_name) && env.existFlag(var_name, __SOCKET_SOTIMEOUT)) {
					found = true;
					stm.setTimeCritical(true);
				}
			}

		}
		if(found && !value_timeout.equals("")){
			this.addConstraint(value_timeout, stm);
			found = false;
		}

	}

	@Override
	public void nextMethod(ASTMethod method, Env env) {

	}

	@Override
	public void nextConstructor(ASTConstructor method, Env env) {

	}

}
