package IntermediateModel.structure;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTConstructor implements IASTMethod {
	String name;
	List<ASTVariable> parameters;

	public ASTConstructor(String name, List<ASTVariable> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}


	public List<ASTVariable> getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTConstructor)) return false;
		ASTConstructor astMethod = (ASTConstructor) o;
		if (getName() != null ? !getName().equals(astMethod.getName()) : astMethod.getName() != null) return false;
		if (getParameters() != null ? !getParameters().equals(astMethod.getParameters()) : astMethod.getParameters() != null)
			return false;
		return true;
	}

	public String toString(){
		String out;
		out = "\t" + name + "(";
		for(ASTVariable v: parameters){
			out += v.toString() + ",";
		}
		if(parameters.size() > 0){
			out = out.substring(0,out.length()-1);
		}
		out += ")\n";
		return out;
	}
}
