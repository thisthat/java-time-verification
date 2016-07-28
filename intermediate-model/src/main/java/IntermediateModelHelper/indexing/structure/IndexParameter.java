package IntermediateModelHelper.indexing.structure;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class IndexParameter {
	String type;
	String name;

	public IndexParameter() {
	}

	public IndexParameter(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
