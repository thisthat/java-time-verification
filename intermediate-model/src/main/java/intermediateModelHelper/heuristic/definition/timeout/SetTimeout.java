package intermediateModelHelper.heuristic.definition.timeout;


import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeTimeout;
import intermediateModelHelper.heuristic.definition.AnnotatedTypes;
import intermediateModelHelper.heuristic.definition.SearchTimeConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class SetTimeout extends SearchTimeConstraint {

	boolean analysingConstructor;
	boolean foundConstructorSetConnectTimeout;
	boolean currentMethodHasSetTimeOut;
	boolean foundConstructorSetReadTimeout;

	List<TimeTimeout> timeTimeout = TemporalInfo.getInstance().getTimeTimeout();


	private class SetTimeOutMethod extends TimeInfo {
		public SetTimeOutMethod(String className, String methodName, List<String> signature) {
			super(className, methodName, signature);
		}
	}

	List<SetTimeOutMethod> timeOutMethod = new ArrayList<>();

	@Override
	public void setup() {
		this.analysingConstructor = false;
		this.foundConstructorSetConnectTimeout = false;
		this.foundConstructorSetReadTimeout = false;
		this.currentMethodHasSetTimeOut = false;
		timeOutMethod.add( new SetTimeOutMethod("java.net.URLConnection", "setConnectTimeout", Arrays.asList("int") ));
		timeOutMethod.add( new SetTimeOutMethod("java.net.HttpURLConnection", "setConnectTimeout", Arrays.asList("int") ));
		timeOutMethod.add( new SetTimeOutMethod("javax.net.ssl.HttpsURLConnection", "setConnectTimeout", Arrays.asList("int") ));

	}

	@Override
	public void nextMethod(ASTMethod method, Env env) {
		this.analysingConstructor = false;
		this.currentMethodHasSetTimeOut = false;
	}

	@Override
	public void nextConstructor(ASTConstructor method, Env env) {
		this.analysingConstructor = true;
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
		IASTRE expr = stm.getExpression();
		if(expr == null){
			return;
		}
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(isSetTimout(elm)) {
					//if we found a settimeout in the constructor, then we assume that it always be applied.
					if(analysingConstructor){
						foundConstructorSetConnectTimeout = true;
					}
					//otherwise it's valid only for the current method
					currentMethodHasSetTimeOut = true;
				}
				if((foundConstructorSetConnectTimeout || currentMethodHasSetTimeOut) && requireSetTimout(elm)){
					SetTimeout.super.addConstraint("timeout", elm);
				}
			}
		});
	}

	private boolean isSetTimout(ASTMethodCall elm) {
		String pointer, name;
		int nPars;
		pointer = elm.getClassPointed();
		name = elm.getMethodName();
		nPars = elm.getParameters().size();
		if(pointer == null) return false;
		for(SetTimeOutMethod m : timeOutMethod){
			if(m.getClassName().equals(pointer) && m.getMethodName().equals(name) && m.getSignature().size() == nPars)
				return true;
		}
		return false;
	}

	private boolean requireSetTimout(ASTMethodCall elm) {
		String pointer, name;
		int nPars;
		pointer = elm.getClassPointed();
		name = elm.getMethodName();
		nPars = elm.getParameters().size();
		if(pointer == null) return false;
		for(TimeTimeout m : timeTimeout){
			if(m.getClassName().equals(pointer) && m.getMethodName().equals(name) && m.getSignature().size() == nPars)
				return true;
		}
		return false;
	}


}
