package slicing.heuristics;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTAssignment;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTVariableDeclaration;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.heuristic.definition.SearchTimeConstraint;

/**
 * The {@link AssignmentTimeVar} searches for instances of time assignment
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class AssignmentTimeVar extends SearchTimeConstraint {

	@Override
	public void next(ASTRE stm, Env env) {
		//works only on ASTRE
		IASTRE expr = stm.getExpression();
		if(expr == null){
			return;
		}

		//we assume that the variable assigned is already in the environment
		//this is assured by the class that trigger this method
		//we only cover the case of Math.max/min because all the others are already covered
		stm.visit(new DefaultASTVisitor(){
			@Override
			public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
				if(elm.getExpr().isTimeCritical()){
					IASTVar var = env.getVar(elm.getNameString());
					if(var != null){
						var.setTimeCritical(true);
					}
				}
			}

			@Override
			public void enterASTAssignment(ASTAssignment elm) {
				if(elm.getRight().isTimeCritical()){
					IASTRE lexpr = elm.getLeft();
					lexpr.visit(new DefualtASTREVisitor(){
						@Override
						public void enterASTLiteral(ASTLiteral elm) {
							IASTVar var = env.getVar(elm.getValue());
							if(var != null){
								var.setTimeCritical(true);
							}
						}
					});
				}
			}
		});

	}

	private void print(IASTStm stm) {
		System.out.println(" :: Assignment Found @" + stm.getLine());
	}

}
