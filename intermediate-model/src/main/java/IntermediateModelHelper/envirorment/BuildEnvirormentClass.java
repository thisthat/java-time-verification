package IntermediateModelHelper.envirorment;


import com.google.common.annotations.Beta;
import intermediateModel.interfaces.*;
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
 * The design of this class should be stateless. Each method should be embrace the functional approach where we have all the resource
 * for the computation in the parameters. The only exception is the list of type and methods time relevant
 * (but just to avoid to read multiple time the files)
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public class BuildEnvirormentClass {

	private static BuildEnvirormentClass instance = null;// = new BuildEnvirormentClass();
	private static List<String> typeTimeRelevant;// = new ArrayList<>();
	private static List<String> methodTimeRelevant;// = new ArrayList<>();

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
	 * Get the instance with lazy initialization.
	 * @return The singleton
	 */
	public static synchronized BuildEnvirormentClass getInstance() {
		if(instance == null){
			instance = new BuildEnvirormentClass();
		}
		return instance;
	}

	protected BuildEnvirormentClass(){}


	/**
	 * This method add to the environment:
	 * <ul>
	 * <li>Attributes that have a time relevant type</li>
	 * <li>TODO: check imports to collect the time information of that classes</li>
	 * </ul>
	 *
	 * @param _class The class to analyze
	 *
	 */
	@Beta
	public Env buildEnvClass(ASTClass _class) {
		return _buildEnvClass(_class, new Env());
	}
	@Beta
	public Env buildEnvClass(ASTClass _class, Env old) {
		return _buildEnvClass(_class, new Env(old));
	}

	private Env _buildEnvClass(ASTClass _class, Env oldEnv){
		Env where = oldEnv;
		//check over attributes
		for (ASTAttribute a : _class.getAttributes()) {
			a.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(a.getType())))
			);
			where.addVar(a);
		}
		//check over methods
		for(IASTMethod m : _class.getMethods()){
			buildEnvMethod(m, where);
		}
		return where;
	}

	/**
	 * This method checks for all the stms in a method:
	 * <ul>
	 * 	<li>Return of time relevant type</li>
	 * 	<li>Parameters with Time type</li>
	 * </ul>
	 *
	 */
	private void buildEnvMethod(IASTMethod mm, Env where) {
		//put the default method from list
		for(String m : methodTimeRelevant){
			where.addMethodTimeRelevant(m);
		}
		//return type is one of the interesting one - only methods
		if (mm instanceof ASTMethod) {
			ASTMethod m = (ASTMethod) mm;
			String retType = m.getReturnType();
			m.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(retType)))
			);
			where.addMethod(m.getName());
		}
	}

	/**
	 * Checks if the variable has a time related type.
	 * @param v	Variable to check
	 * @return	True if is in the list of those types that are time related.
	 */
	public boolean hasVarTypeTimeRelated(IASTVar v){
		return typeTimeRelevant.stream().anyMatch(type -> (type.equals(v.getType())));
	}

	/**
	 * Checks if the type is a time related type.
	 * @param t	Type to check
	 * @return	True if is in the list of those types that are time related.
	 */
	public boolean isTypeTimeRelated(String t){
		return typeTimeRelevant.stream().anyMatch(type -> (type.equals(t)));
	}


}
