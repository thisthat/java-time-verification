package envirorment;

import IntermediateModel.interfaces.ASTREVisitor;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.*;
import IntermediateModel.structure.expression.ASTAssignment;
import IntermediateModel.structure.expression.ASTAttributeAccess;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTVariableDeclaration;
import IntermediateModel.visitors.DefualtASTREVisitor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class BuildEnvirormentClass {
	private Env env;

	private List<String> typeTimeRelevant;// = new ArrayList<>();

	{
		String f = getClass().getClassLoader().getResource("TypeTimeRelevant.txt").getFile();
		try {
			typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (IOException e) {
			typeTimeRelevant = new ArrayList<>();
		}
	}

	public BuildEnvirormentClass() {
		env = new Env();
	}

	public BuildEnvirormentClass(Env env) {
		this.env = env;
	}


	/**
	 * This method add to the envirorment:
	 * <ul>
	 * <li>Attributes that have a time relevant type</li>
	 * <li>TODO: check imports to collect the time information of that classes</li>
	 * </ul>
	 *
	 * @param _class The class to analyze
	 * @return The {@link Env} fulfill with the Info for parsing
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
	 * @param m Method to annotate
	 */
	private void buildEnvMethod(IASTMethod mm) {
		//return type is one of the interesting one - only methods
		if (mm instanceof ASTMethod) {
			ASTMethod m = (ASTMethod) mm;
			String retType = m.getReturnType();
			m.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(retType)))
			);
			if (m.isTimeCritical()) {
				env.addMethod(m, new Env());
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
	 * @return The envirorment (maybe) enriched.
	 */
	public void checkRE(ASTRE rexp, Env env){
		rexp.getExpression().visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
				setVariableInEnv(elm, env);
			}
		});
	}

	public void setVariableInEnv(ASTVariable v, Env where){
		v.setTimeCritical(
				typeTimeRelevant.stream().anyMatch(type -> (type.equals(v.getType())))
		);
		if(v.isTimeCritical()){
			where.addVar(v);
		}
	}
	public void setVariableInEnv(ASTVariableDeclaration v, Env where){
		setVariableInEnv(new ASTVariable(v.startToken,v.endToken, v.getNameString(), v.getType()), where);
	}

	/**
	 * Return the envirorment
	 * @return {@link Env} structure
	 */
	public Env getEnv() {
		return env;
	}
}
