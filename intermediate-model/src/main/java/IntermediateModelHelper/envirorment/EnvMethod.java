package IntermediateModelHelper.envirorment;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
class EnvMethod {
	private String name;
	private boolean istimeRelevant;

	public EnvMethod(String name) {
		this.name = name;
	}

	public EnvMethod(String name, boolean istimeRelevant) {
		this.name = name;
		this.istimeRelevant = istimeRelevant;
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
