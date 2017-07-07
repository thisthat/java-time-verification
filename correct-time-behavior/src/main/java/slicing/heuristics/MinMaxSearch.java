package slicing.heuristics;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.heuristic.definition.SearchTimeConstraint;

/**
 * The {@link MinMaxSearch} searches for instances of time assignment
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class MinMaxSearch extends SearchTimeConstraint {

		@Override
	public void next(ASTRE stm, Env env) {

		if(stm.getExpression() == null){
			return;
		}
		stm.getExpression().visit(new DefualtASTREVisitor() {
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(elm.getClassPointed() != null && elm.getClassPointed().equals("java.lang.Math")){
					String name = elm.getMethodName();
					if(name.equals("min") || name.equals("max")){
						checkForTime(elm, env);
					}
				}
			}
		});
	}

	private void checkForTime(ASTMethodCall elm, Env env) {
		//if(1==1) return;
		for(IASTRE exp : elm.getParameters()){
			if(CheckExpression.checkRightHandAssignment(exp, env)){
				elm.setTimeCritical(true);
				System.out.println("Found max/min @" + elm.getLine());
			}
		}
	}


}
