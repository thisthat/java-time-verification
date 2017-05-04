package intermediateModelHelper.heuristic.definition;

import com.rits.cloning.Cloner;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTAssignment;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.CheckExpression;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;
import intermediateModelHelper.heuristic.beta.Translation;

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
public class AssignmentTimeVar extends SearchTimeConstraint {

	@Override
	public void setup(ASTClass c) {
		super.setup(c);
	}

	@Override
	public void next(ASTRE stm, Env env) {
		//works only on ASTRE
		IASTRE expr = stm.getExpression();
		if(expr == null){
			return;
		}

		if(stm.getLine() == 507){
			System.out.println("BRK");
		}
		if(stm.getLine() == 508){
			System.out.println("BRK");
		}
		if(stm.getLine() == 509){
			System.out.println("BRK");
		}

		if(CheckExpression.checkRE(stm,env)){
			stm.markResetTime();
		}



		//search for A {<,<=,>,>=} C
		expr.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTAssignment(ASTAssignment elm) {
				IASTRE l = elm.getLeft();
				if(l instanceof ASTLiteral){
					String varName = ((ASTLiteral) l).getValue();
					IASTVar v = env.getVar(varName);
					if(v != null && v.isTimeCritical()){
						addConstraint(elm, env);
					}
				}
			}
		});

	}

	protected void addConstraint(ASTAssignment stm, Env e) {
		super.addConstraint(stm.print(), stm);
		//Constraint edgeVersion = cloner.deepClone(c);
		//c.setEdgeVersion(edgeVersion);
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
