package IntermediateModelHelper.envirorment;


import com.google.common.annotations.Beta;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.expression.ASTMethodCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class handle kinda of a symbol table for dynamic scoping.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Env {

	private Env prev;
	private List<IASTVar> varList = new ArrayList<>();;
	private Map<String, ArrayList<Integer>> flags = new HashMap<>();
	private List<EnvMethod> methodList = new ArrayList<>();

	/**
	 * We create an Environment that is the first one
	 */
	public Env() {
		//Empty one
		this.prev = null;
	}

	/**
	 * Create the Object on the top of the previous Env
	 * @param prev	Previous environment
	 */
	public Env(Env prev) {
		this.prev = prev;
	}

	/**
	 * Get the list of <u>ALL</u> variables of the current Env.
	 * It does not look in the previous ones.
	 * @return return a list of {@link IASTVar}s
	 */
	public List<IASTVar> getVarList() {
		return varList;
	}

	/**
	 * Ge the previous Env (could be null)
	 * @return The Env where we build on the top of
	 */
	public Env getPrev() {
		return prev;
	}

	/**
	 * Check in the current Env and in the previous ones if there exists a variable with the given name.
	 * @param v	Name of the variable to search
	 * @return	Boolean value corresponding if there exist or not the var in the stack of Envs.
	 */
	public boolean existVarName(String v){
		for(IASTVar vEnv : varList){
			if(vEnv.getName().equals(v))
				return true;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existVarName(v);
		} else {
			return false;
		}
	}

	/**
	 * Check in the current Env and in the previous ones if there exists a variable with the given name.
	 * <b>The variable must be time related!</b>
	 * @param v	Name of the variable to search
	 * @return	Boolean value corresponding if there exist or not the var time related in the stack of Envs.
	 */
	public boolean existVarNameTimeRelevant(String v){
		for(IASTVar vEnv : varList){
			if(vEnv.getName().equals(v)) {
				return vEnv.isTimeCritical();
			}
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existVarNameTimeRelevant(v);
		} else {
			return false;
		}
	}

	/**
	 * Get a variable from the Env by its name.
	 * The search is performed also in the previous Envs.
	 * @param v	Name of the variable to retrieve
	 * @return	The Variable
	 */
	public IASTVar getVar(String v){
		for(IASTVar vEnv : varList){
			if(vEnv.getName().equals(v))
				return vEnv;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.getVar(v);
		} else {
			return null;
		}
	}

	/**
	 * The following method could be changed.
	 * Given a variable name it inserts a flag in the set of flags of that particular variable.
	 * <b>It is assumed that the variable exists in the Env</b>
	 * @param v			Name of the variable
	 * @param flag		Flag to add
	 */
	@Beta
	public void addFlag(String v, Integer flag){
		Env correct_env = getCorrectEnv(v);

		ArrayList<Integer> fv = new ArrayList<>();
		if(correct_env.getFlags().containsKey(v)){
			fv = correct_env.getFlags().get(v);
		}
		fv.add(flag);
		correct_env.getFlags().put(v, fv);
	}

	/**
	 * Helper method for {@link Env#addFlag(String, Integer)}.
	 * It retrives the Env where the particular variable was inserted.
	 * @param v	Variable name to search
	 * @return	Environment where the variable is defined
	 */
	private Env getCorrectEnv(String v) {
		for(IASTVar vEnv : varList){
			if(vEnv.getName().equals(v))
				return this;
		}
		//is not here, search in the previous ones
		return prev.getCorrectEnv(v);
	}


	/**
	 * Check if a particular flag for a variable is defined.
	 * @param v		Variable name
	 * @param flag	Flag that the variable should have
	 * @return		True in the case the variable has the flag
	 */
	public boolean existFlag(String v, Integer flag){
		ArrayList<Integer> fv = new ArrayList<>();
		if(flags.containsKey(v)){
			fv = flags.get(v);
		}
		if(fv.contains(flag)){
			return true;
		} else {
			return prev != null && this.prev.existFlag(v, flag);
		}
	}

	/**
	 * Get the first variable with the given type
	 * @param v	Type to looking for
	 * @return	Null or the Var (it could be not exists)
	 */
	public IASTVar getVarByType(String v){
		for(IASTVar vEnv : varList){
			if(vEnv.getType().equals(v))
				return vEnv;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.getVarByType(v);
		} else {
			return null;
		}
	}

	/**
	 * Retrieve all variables with a given type
	 * @param type	Type that we are interested on
	 * @return		List of variables
	 */
	public List<IASTVar> getVarsByType(String type){
		List<IASTVar> out = new ArrayList<>();
		for(IASTVar vEnv : varList){
			if(vEnv.getType().equals(type))
				out.add(vEnv);
		}
		if(prev != null){
			out.addAll(prev.getVarsByType(type));
		}
		return out;
	}

	/**
	 * Search if it exists a method inside the env
	 * @param methodName	Name of the method that we are looking for
	 * @return				True if it exists in the Env
	 */
	@Beta
	public boolean existMethod(String methodName){
		for(EnvMethod mm : methodList){
			String method = mm.getName();
			if(method.equals(methodName)) {
				return true;
			}
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethod(methodName);
		} else {
			return false;
		}
	}

	/**
	 * Search if it exists a method time related inside the env
	 * @param methodName	Name of the method that we are looking for
	 * @return				True if it exists in the Env
	 */
	@Beta
	public boolean existMethodTimeRelevant(String methodName){
		EnvMethod mEnv = new EnvMethod(methodName);
		if(methodList.contains(mEnv)){
			boolean flag = false;
			for(EnvMethod mm : methodList){
				if(mm.getName().equals(methodName)){
					flag = mm.istimeRelevant();
				}
			}
			return flag;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethodTimeRelevant(methodName);
		} else {
			return false;
		}
	}

	/**
	 * Insert a Var in the current Env.
	 * If it is already in the list we have just to update the time related condition.
	 * @param v	Variable in object
	 */
	public void addVar(IASTVar v){
		if(varList.contains(v)){
			IASTVar oldvar = getVar(v.getName());
			oldvar.setTimeCritical( oldvar.isTimeCritical() || v.isTimeCritical() );
		} else {
			varList.add(v);
		}
	}

	/**
	 * Add to the Environment a new Method
	 * @param method	Method to add
	 */
	@Beta
	public void addMethod(String method){
		methodList.add( new EnvMethod(method));
	}

	/**
	 * Add to the Environment a new Method that is time related
	 * @param method	Method name to add
	 */
	@Beta
	public void addMethodTimeRelevant(String method){
		EnvMethod m = new EnvMethod(method);
		m.setIstimeRelevant(true);
		methodList.add( m );
	}

	/**
	 * Return the list of flag of all the variables.
	 * The list is <b>only of the current Environment</b>.
	 * @return List of flags for the variables of the current environment.
	 */
	public Map<String, ArrayList<Integer>> getFlags() {
		return flags;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Env)) return false;

		Env env = (Env) o;

		if (prev != null ? !prev.equals(env.prev) : env.prev != null) return false;
		if (varList != null ? !varList.equals(env.varList) : env.varList != null) return false;
		if (methodList != null ? !methodList.equals(env.methodList) : env.methodList != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = prev != null ? prev.hashCode() : 0;
		result = 31 * result + (varList != null ? varList.hashCode() : 0);
		result = 31 * result + (methodList != null ? methodList.hashCode() : 0);
		return result;
	}
}
