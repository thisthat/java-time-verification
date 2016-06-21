package IntermediateModelHelper.envirorment;


import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.*;
import intermediateModel.visitors.DefualtASTREVisitor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class create the a small symbol table to keep track of the execution state with a sort of <i>symbolic execution</i>
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 *
 */
public class BuildEnvirormentClass {
	private Env env;

	public static List<String> typeTimeRelevant;// = new ArrayList<>();
	public static List<String> methodTimeRelevant;// = new ArrayList<>();

	{
		String f = getClass().getClassLoader().getResource("descriptorTimeRelevant/TypeTimeRelevant.txt").getFile();
		try {
			typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (IOException e) {
			typeTimeRelevant = new ArrayList<>();
		}
		f = getClass().getClassLoader().getResource("descriptorTimeRelevant/MethodTimeRelevant.txt").getFile();
		try {
			methodTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (IOException e) {
			methodTimeRelevant = new ArrayList<>();
		}
	}

	/**
	 * It creates the object with a new empty envirorment;
	 */
	public BuildEnvirormentClass() {
		env = new Env();
	}

	/**
	 * It creates the object with a new envirorment with the one passed as parameter.
	 * @param env {@link Env} class to use
	 */
	public BuildEnvirormentClass(Env env) {
		this.env = env;
	}


	/**
	 * This method add to the IntermediateModelHelper.envirorment:
	 * <ul>
	 * <li>Attributes that have a time relevant type</li>
	 * <li>@TODO check imports to collect the time information of that classes</li>
	 * </ul>
	 *
	 * @param _class The class to analyze
	 *
	 */

	public void buildEnvClass(ASTClass _class) {
		//check over attributes
		for (ASTAttribute a : _class.getAttributes()) {
			a.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(a.getType())))
			);
			if (a.isTimeCritical()) {
				env.addVar(a);
			}

		}
		//check over methods
		for (IASTMethod m : _class.getMethods()) {
			buildEnvMethod(m);
		}
	}

	/**
	 * This method checks for all the stms in a method:
	 * <ul>
	 * 	<li>Return of time relevant type</li>
	 * 	<li>Parameters with Time type</li>
	 * </ul>
	 *
	 */
	private void buildEnvMethod(IASTMethod mm) {
		//put the default method from list
		for(String m : methodTimeRelevant){
			env.addMethod(m, new Env());
		}
		//return type is one of the interesting one - only methods
		if (mm instanceof ASTMethod) {
			ASTMethod m = (ASTMethod) mm;
			String retType = m.getReturnType();
			m.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(retType)))
			);
			if (m.isTimeCritical()) {
				env.addMethod(m.getName(), new Env());
			}
		}
	}

	/**
	 * This method add to the definition of variable of the Env passed as parameter all the time relevant information.
	 * @param lPars List of parameters to check
	 * @param env {@link Env} class where to add the new definition
	 * @return The envirorment (maybe) enriched.
	 */
	public Env checkPars(List<ASTVariable> lPars, Env env){
		for(ASTVariable v : lPars){
			setVariableInEnv(v, env);
		}
		return env;
	}

	/**
	 * This method will go through the definition of a RExpr and check for the following:
	 * <ul>
	 *     <li>Definition of new vars</li>
	 * </ul>
	 * If something is time related, it will be added to the Env that is returned.
	 * @param rexp {@link ASTRE} to check
	 * @param env {@link Env} class where to add the new definition
	 *
	 */
	public void checkRE(ASTRE rexp, Env env){
		if(rexp == null){
			return;
		}
		rexp.getExpression().visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
				setVariableInEnv(elm, env);
			}
		});
	}

	/**
	 * The method accept a {@link ASTVariable} and an {@link Env} and checks if is time
	 * relevant. In the positive case, it adds to the envirorment the variable.
	 * @param v			Variable to control
	 * @param where		Envirorment where to add
	 */
	public void setVariableInEnv(ASTVariable v, Env where){
		v.setTimeCritical(
				typeTimeRelevant.stream().anyMatch(type -> (type.equals(v.getType())))
		);
		if(v.isTimeCritical()){
			where.addVar(v);
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
	public void setVariableInEnv(ASTVariableDeclaration v, Env where){
		//check the type
		setVariableInEnv(new ASTVariable(v.start,v.end, v.getNameString(), v.getType()), where);
		//check the expr
		if(v.getExpr() != null) {
			v.getExpr().visit(new DefualtASTREVisitor() {
				//method call

				@Override
				public void enterASTMethodCall(ASTMethodCall elm) {
					if(where.existMethod( elm )){
						ASTVariable vv = new ASTVariable(v.start,v.end, v.getNameString(), v.getType());
						where.addVar(vv);
						v.setTimeCritical(true);
						vv.setTimeCritical(true);
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
							if(checkIt(elm, where)){
								v.setTimeCritical(true);
								ASTVariable vv = new ASTVariable(v.start,v.end, v.getNameString(), v.getType());
								vv.setTimeCritical(true);
								where.addVar(vv);
							}
					}
				}
			});
		}
	}

	/**
	 * The method accept a {@link ASTBinary} expression and an {@link Env}.
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
	public static boolean checkIt(ASTBinary elm, Env where) {
		final boolean[] r = {false};
		elm.visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTLiteral(ASTLiteral elm) {
				if(where.existVarName(elm.getValue()))
					r[0] = true;
			}

			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				if(where.existMethod(elm)){
					r[0] = true;
				}
			}

			@Override
			public void enterASTMultipleMethodCall(ASTMultipleMethodCall elm) {
				if(elm.getVariable() != null && elm.getVariable() instanceof ASTLiteral){
					if( where.existVarName(((ASTLiteral) elm.getVariable()).getValue()) ){
						r[0] = true;
					}
				}
			}
		});
		return r[0];
	}

	/**
	 * Return the IntermediateModelHelper.envirorment
	 * @return {@link Env} structure
	 */
	public Env getEnv() {
		return env;
	}
}
