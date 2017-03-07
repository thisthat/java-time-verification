package intermediateModelHelper.heuristic.definition;

import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporal.structure.TimeTimeout;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;

import java.util.List;


/**
 *
 * TODO: Define how process user annotations.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnnotatedTypes extends SearchTimeConstraint {

	List<TimeMethod>  timeMethods = TemporalInfo.getInstance().getTimeMethods();
	List<TimeTimeout> timeTimeout = TemporalInfo.getInstance().getTimeTimeout();
	List<TimeTypes>   timeTypes   = TemporalInfo.getInstance().getTimeTypes();


	public AnnotatedTypes() {
	}

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
		if(expr == null){
			return;
		}
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				String pointer = elm.getClassPointed();
				if(pointer != null && containTimeOut(pointer, elm.getMethodName(), elm.getParameters().size())) {
					AnnotatedTypes.super.addConstraint("timeout", elm);
				}
			}
		});
	}

	private boolean containTimeOut(String pointer, String name, int nPars){
		for(TimeMethod m : timeMethods){
			if(m.getClassName().equals(pointer) && m.getMethodName().equals(name) && m.getSignature().size() == nPars)
				return true;
		}
		return false;
	}

}
