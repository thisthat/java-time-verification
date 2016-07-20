package IntermediateModelHelper.envirorment;


import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.expression.ASTMethodCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class handle a kinda of a symbol table for dynamic scoping.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Env {

	protected class EnvMethod {
		private String name;
		private boolean istimeRelevant;

		public EnvMethod(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public boolean istimeRelevant() {
			return istimeRelevant;
		}

		public void setIstimeRelevant(boolean istimeRelevant) {
			this.istimeRelevant = istimeRelevant;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof EnvMethod)) return false;

			EnvMethod envMethod = (EnvMethod) o;

			return getName() != null ? getName().equals(envMethod.getName()) : envMethod.getName() == null;

		}

		@Override
		public int hashCode() {
			return getName() != null ? getName().hashCode() : 0;
		}
	}

	private Env prev;
	private List<IASTVar> varList;
	private Map<String, ArrayList<Integer>> flags = new HashMap<>();
	private List<EnvMethod> methodList;

	{
		varList = new ArrayList<>();
		methodList = new ArrayList<>();
	}

	public Env() {
		//Empty one
		this.prev = null;
	}

	public Env(Env prev) {
		this.prev = prev;
	}

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

	public void addFlag(String v, Integer flag){
		ArrayList<Integer> fv = new ArrayList<>();
		if(flags.containsKey(v)){
			fv = flags.get(v);
		}
		fv.add(flag);
		flags.put(v, fv);
	}

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

	public IASTVar getLastVarByTime(String v){
		for(IASTVar vEnv : varList){
			if(vEnv.getType().equals(v))
				return vEnv;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.getLastVarByTime(v);
		} else {
			return null;
		}
	}

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

	public boolean existMethod(ASTMethod m){
		EnvMethod mEnv = new EnvMethod(m.getName());
		if(methodList.contains(mEnv)){
			return true;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethod(m);
		} else {
			return false;
		}
	}

	public boolean existMethodTimeRelevant(ASTMethod m){
		EnvMethod mEnv = new EnvMethod(m.getName());
		if(methodList.contains(mEnv)){
			boolean flag = false;
			for(EnvMethod mm : methodList){
				if(mm.getName().equals(m.getName())){
					flag = mm.istimeRelevant();
				}
			}
			return flag;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethodTimeRelevant(m);
		} else {
			return false;
		}
	}

	public boolean existMethod(ASTMethodCall m){
		for(EnvMethod mm : methodList){
			String method = mm.getName();
			if(method.equals(m.getMethodName())) {
				return true;
			}
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethod(m);
		} else {
			return false;
		}
	}

	public boolean existMethodTimeRelevant(ASTMethodCall m){
		EnvMethod mEnv = new EnvMethod(m.getMethodName());
		if(methodList.contains(mEnv)){
			boolean flag = false;
			for(EnvMethod mm : methodList){
				if(mm.getName().equals(m.getMethodName())){
					flag = mm.istimeRelevant();
				}
			}
			return flag;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethodTimeRelevant(m);
		} else {
			return false;
		}
	}

	public boolean existMethod(String m){
		for(EnvMethod mm : methodList){
			String method = mm.getName();
			if(method.equals(m)) {
				return true;
			}
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethod(m);
		} else {
			return false;
		}
	}

	public boolean existMethodTimeRelevant(String m){
		EnvMethod mEnv = new EnvMethod(m);
		if(methodList.contains(mEnv)){
			boolean flag = false;
			for(EnvMethod mm : methodList){
				if(mm.getName().equals(m)){
					flag = mm.istimeRelevant();
				}
			}
			return flag;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethodTimeRelevant(m);
		} else {
			return false;
		}
	}


	public void addVar(IASTVar v){
		if(varList.contains(v)){
			varList.remove(v);
		}
		varList.add(v);
	}

	public void addMethod(String v, Env e){
		methodList.add( new EnvMethod(v));
	}
	public void addMethodTimeRelevant(String v, Env e){
		EnvMethod m = new EnvMethod(v);
		m.setIstimeRelevant(true);
		methodList.add( m );
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
