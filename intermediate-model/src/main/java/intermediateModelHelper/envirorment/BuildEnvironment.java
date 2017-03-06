package intermediateModelHelper.envirorment;


import com.google.common.annotations.Beta;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTAttribute;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTMethod;

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
public class BuildEnvironment {

	private static BuildEnvironment instance = null;// = new BuildEnvironment();
	private static List<String> typeTimeRelevant;// = new ArrayList<>();
	private static List<String> methodTimeRelevant;// = new ArrayList<>();

	/**
	 * Get the instance with lazy initialization.
	 * @return The singleton
	 */
	public static synchronized BuildEnvironment getInstance() {
		if(instance == null){
			instance = new BuildEnvironment();
		}
		return instance;
	}

	protected BuildEnvironment(){
		try {
			String path = BuildEnvironment.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			path = path.substring(0, path.lastIndexOf("/")) + "/conf/";
			String f = path + "TypeTimeRelevant.txt";
			typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
			f = path + "MethodTimeRelevant.txt";
			methodTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
		} catch (Exception e) {
			//System.err.println("Cannot read from FS. Trying to populate time relevant information from resources.");
			//try to load from resources
			String f = getClass().getClassLoader().getResource("descriptorTimeRelevant/TypeTimeRelevant.txt").getFile();
			try {
				typeTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
			} catch (IOException e1) {
				typeTimeRelevant = new ArrayList<>();
			}
			f = getClass().getClassLoader().getResource("descriptorTimeRelevant/MethodTimeRelevant.txt").getFile();
			try {
				methodTimeRelevant = java.nio.file.Files.readAllLines(Paths.get(f));
			} catch (IOException e1) {
				methodTimeRelevant = new ArrayList<>();
			}

		}
	}


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
		return _buildEnvClass(_class, new EnvBase());
	}
	@Beta
	public Env buildEnvClass(ASTClass _class, Env old) {
		return _buildEnvClass(_class, old);
	}

	private Env _buildEnvClass(ASTClass _class, Env where){
		//Env where = oldEnv;
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
			String[] exp = m.split(",");
			List<String> signature = new ArrayList<>();
			for(int i = 2; i < exp.length; i++){
				signature.add(exp[i]);
			}
			where.addMethodTimeRelevant(exp[0], exp[1], signature);
		}
		//return type is one of the interesting one - only methods
		if (mm instanceof ASTMethod) {
			ASTMethod m = (ASTMethod) mm;
			String retType = m.getReturnType();
			m.setTimeCritical(
					typeTimeRelevant.stream().anyMatch(type -> (type.equals(retType)))
			);
			where.addMethod(m.getName(), m.getReturnType(), m.getSignature());
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
