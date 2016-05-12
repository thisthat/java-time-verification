package IntermediateModel.structure;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTMethod {
	String name;
	String returnType;
	List<ASTVariable> parameters;

	public ASTMethod(String name, String returnType, List<ASTVariable> parameters) {
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public List<ASTVariable> getParameters() {
		return parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTMethod)) return false;
		ASTMethod astMethod = (ASTMethod) o;
		if (getName() != null ? !getName().equals(astMethod.getName()) : astMethod.getName() != null) return false;
		if (getReturnType() != null ? !getReturnType().equals(astMethod.getReturnType()) : astMethod.getReturnType() != null)
			return false;
		if (getParameters() != null ? !getParameters().equals(astMethod.getParameters()) : astMethod.getParameters() != null)
			return false;
		return true;
	}
}
