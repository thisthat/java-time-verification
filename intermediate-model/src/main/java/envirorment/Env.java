package envirorment;


import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Env {
	private Env prev;
	private List<IASTVar> varList;
	private Map<ASTMethod, Env> methodList;

	{
		varList = new ArrayList<>();
		methodList = new HashMap<>();
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

	public boolean existMethod(ASTMethod m){
		if(methodList.containsKey(m)){
			return true;
		}
		//is not here, search in the previous ones
		if(prev != null){
			return prev.existMethod(m);
		} else {
			return false;
		}
	}


	public void addVar(IASTVar v){
		varList.add(v);
	}

	public void addMethod(ASTMethod v, Env e){
		methodList.put(v, e);
	}
}
