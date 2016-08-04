package IntermediateModelHelper.heuristic.definition;

import IntermediateModelHelper.CheckExpression;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.visitors.DefualtASTREVisitor;
import IntermediateModelHelper.envirorment.Env;

/**
 * The heuristic searches for snippet of code in a guard section of the following type:
 * <pre>
 * {@code  var - x < y | var < y }
 * </pre>
 * Where var is a variable that has a type time related. Regarding the x and y, they can either be constant
 * values or be time related variables.
 * The search is not limited to the <i>&lt;</i> operator, it searches for all the order operators.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class TimeoutResources extends SearchTimeConstraint {

	@Override
	public void next(IASTStm stm, Env env) {
		if(!(stm instanceof ASTRE)) return;
		//works only on ASTRE
		IASTRE expr = ((ASTRE) stm).getExpression();

		final boolean[] found = {false};
		//search for A {<,<=,>,>=} C
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTbinary(ASTBinary elm) {
				switch (elm.getOp()){
					case less:
					case lessEqual:
					case greater:
					case greaterEqual:
						if(CheckExpression.checkIt(elm, env)){
							stm.setTimeCritical(true);
							found[0] = true;
						}
				}
			}
		});

		if(found[0]){
			this.addConstraint(stm.getCode(),stm);
		}

	}

	/*
	private boolean checkIt(ASTBinary expr, Env env){
		final boolean[] r = {false};
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTLiteral(ASTLiteral elm) {
				if(env.existVarName(elm.getValue()))
					r[0] = true;
			}

			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(env.existMethod(elm)){
					r[0] = true;
				}
			}

			@Override
			public void enterASTMultipleMethodCall(ASTMultipleMethodCall elm) {
				if(elm.getVariable() != null && elm.getVariable() instanceof ASTLiteral){
					if( env.existVarName(((ASTLiteral) elm.getVariable()).getValue()) ){
						r[0] = true;
					}
				}
			}
		});
		return r[0];
	}
	*/
}
