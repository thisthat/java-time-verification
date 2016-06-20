package heuristic;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTBinary;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import IntermediateModel.structure.expression.ASTMultipleMethodCall;
import IntermediateModel.visitors.DefualtASTREVisitor;
import envirorment.BuildEnvirormentClass;
import envirorment.Env;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The following class implement a search in the ASTRE of an expression of type
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
						if(BuildEnvirormentClass.checkIt(elm, env)){
							stm.setTimeCritical(true);
							found[0] = true;
						}
				}
			}
		});

		if(found[0]){
			this.addConstraint(stm);
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

	@Override
	public void next(IASTRE expr, Env env) {

	}
}
