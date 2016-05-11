package IntermediateModel.structure;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTClass {

	public enum Visibility {
		PUBLIC,
		PROTECT,
		ABSTRACT,
		FINAL,
		PRIVATE,
		STRICTFP
	}

	List<String> methods;
	String name;
	Visibility accessRight;

	public ASTClass(String name, Visibility accessRight){
		this.name = name;
		this.accessRight = accessRight;
	}

	public ASTClass(String name, Visibility accessRight, List<String> methods) {
		this.methods = methods;
		this.name = name;
		this.accessRight = accessRight;
	}

	public List<String> getMethods() {
		return methods;
	}

	public String getName() {
		return name;
	}

	public Visibility getAccessRight() {
		return accessRight;
	}

	public void addMethod(String method){
		methods.add(method);
	}


}
