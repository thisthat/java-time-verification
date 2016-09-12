package IntermediateModelHelper;

import IntermediateModelHelper.envirorment.BuildEnvironment;
import IntermediateModelHelper.envirorment.Env;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.ASTVariable;
import intermediateModel.structure.expression.*;
import intermediateModel.visitors.DefualtASTREVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CheckExpression {

	/**
	 * This method add to the definition of variable of the Env passed as parameter all the time relevant information.
	 * @param lPars List of parameters to check
	 * @param env {@link Env} class where to add the new definition
	 * @return The envirorment (maybe) enriched.
	 */
	public static Env checkPars(List<ASTVariable> lPars, Env env){
		for(ASTVariable v : lPars){
			setVariableInEnv(v, env);
		}
		return env;
	}

	/**
	 * This method will go through the definition of a RExpr and check for the following:
	 * <ul>
	 *     <li>Definition of new vars</li>
	 *     <li>Assignment of a value to a var/li>
	 * </ul>
	 * If something is time related, it will be added to the Env that is returned.
	 * @param rexp {@link ASTRE} to check
	 * @param env {@link Env} class where to add the new definition
	 *
	 */
	public static Env checkRE(ASTRE rexp, Env env){
		if(rexp == null){
			return env;
		}
		rexp.getExpression().visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
				setVariableInEnv(elm, env);
			}

			@Override
			public void enterASTAssignment(ASTAssignment elm) {
				setVariableInEnv(elm, env);
			}
		});
		return env;
	}

	/**
	 * The method accept a {@link ASTVariable} and an {@link Env} and checks if is time
	 * relevant. In the positive case, it adds to the envirorment the variable.
	 * @param v			Variable to control
	 * @param where		Envirorment where to add
	 */
	public static Env setVariableInEnv(ASTVariable v, Env where){
		v.setTimeCritical(
				BuildEnvironment.getInstance().hasVarTypeTimeRelated(v)
		);
		where.addVar(v);
		return where;
	}

	/**
	 * The method accept a {@link ASTVariableDeclaration} and an {@link Env}.
	 * It checks if the varible is a time relevant type and in the positive case it adds the var to the envirorment.
	 * Moreover, it checks if the expression of inizialization is a time relevant expression.
	 * The checks that the method performs are regarding to:
	 * <ul>
	 *     <li>There is a call to a method that is time relevant</li>
	 *     <li>There is a math expression that involves time relevant variable</li>
	 * </ul>
	 * @param v			Variable to check
	 * @param where		Envirorment where to add
	 */
	private static void setVariableInEnv(ASTVariableDeclaration v, Env where){
		//check the type
		ASTVariable var = new ASTVariable(v.getStart(),v.getEnd(), v.getNameString(), v.getType());
		var.setSouceCode(v.getCode());
		var.setTimeCritical(v.isTimeCritical());
		setVariableInEnv(var, where);
		//check the expr
		if(v.getExpr() != null) {
			v.getExpr().visit(new DefualtASTREVisitor() {
				//method call

				@Override
				public void enterASTMethodCall(ASTMethodCall elm) {
					if(where.existMethodTimeRelevant( elm.getMethodName(), getSignature(elm.getParameters(), where) )){
						v.setTimeCritical(true);
						var.setTimeCritical(true);
						where.addVar(var);
					}
				}

				//math op between time
				@Override
				public void enterASTbinary(ASTBinary elm) {
					switch (elm.getOp()){
						case minus:
						case plus:
						case mul:
						case div:
						case mod:
							if(checkIt(elm, where)){
								v.setTimeCritical(true);
								var.setTimeCritical(true);
								where.addVar(var);
							}
					}
				}
			});
		}
	}

	/**
	 * The method accept a {@link ASTVariableDeclaration} and an {@link Env}.
	 * It checks if the varible is a time relevant type and in the positive case it adds the var to the envirorment.
	 * Moreover, it checks if the expression of inizialization is a time relevant expression.
	 * The checks that the method performs are regarding to:
	 * <ul>
	 *     <li>There is a call to a method that is time relevant</li>
	 *     <li>There is a math expression that involves time relevant variable</li>
	 * </ul>
	 * @param v			Variable to check
	 * @param where		Envirorment where to add
	 */
	private static void setVariableInEnv(ASTAssignment v, Env where){
		IASTRE left = v.getLeft();
		if(left instanceof ASTLiteral){
			String name = ((ASTLiteral) left).getValue();
			IASTVar var = where.getVar(name);
			if(var != null //should be never the case if code compiles
					&& checkIt(v.getRight(), where)){ //if exists something time related
				var.setTimeCritical(true); //the assigned var is time relevant
				v.getRight().visit(new DefualtASTREVisitor() { //and also all the var used inside the expr
					@Override
					public void enterASTLiteral(ASTLiteral elm) {
						IASTVar var = where.getVar(elm.getValue());
						if(var != null) //avoid method call that can be literal as well
							var.setTimeCritical(true);
					}
				});
			}
		}
	}

	/**
	 * The method accept a {@link IASTRE} expression and an {@link Env}.
	 * It checks if the expression is a time relevant expression.
	 * The checks that the method performs are regarding to:
	 * <ul>
	 *     <li>There is a call to a method that is time relevant</li>
	 *     <li>There is a usage of a name of variable that is time relevant</li>
	 * </ul>
	 * @param elm	The expression where to perform the check
	 * @param where	The envirorment to use for the search
	 * @return		true if the expression contains time relevant objects
	 */
	public static boolean checkIt(IASTRE elm, Env where) {
		final boolean[] r = {false};
		elm.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTLiteral(ASTLiteral literal) {
				if(where.existVarNameTimeRelevant(literal.getValue()) ) //and time critical
					r[0] = true;
			}

			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(where.existMethodTimeRelevant(elm.getMethodName(), getSignature(elm.getParameters(), where))){
					r[0] = true;
				}
			}

		});
		return r[0];
	}

	private static List<String> getSignature(List<IASTRE> parameters, Env where){
		List<String> out = new ArrayList<>();
		for(IASTRE p : parameters){
			out.add(where.getExprType(p));
		}
		return out;
	}
}
