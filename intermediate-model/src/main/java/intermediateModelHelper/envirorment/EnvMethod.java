package intermediateModelHelper.envirorment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
class EnvMethod {
	private String name;
	private String retType;
	private boolean istimeRelevant;
	private List<String> signature = new ArrayList<>();

	public EnvMethod(String name, String retType, List<String> signature) {
		this.name = name;
		this.retType = retType;
		this.signature = signature;
	}

	public EnvMethod(String name, List<String> signature) {
		this.name = name;
		this.signature = signature;
	}

	public void setSignature(List<String> signature) {
		this.signature = signature;
	}

	public EnvMethod(String name, boolean istimeRelevant) {
		this.name = name;
		this.istimeRelevant = istimeRelevant;
	}

	public String getRetType() {
		return retType;
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

		if (getName() != null ? !getName().equals(envMethod.getName()) : envMethod.getName() != null) return false;
		return signature != null ? signature.equals(envMethod.signature) : envMethod.signature == null;

	}

}
