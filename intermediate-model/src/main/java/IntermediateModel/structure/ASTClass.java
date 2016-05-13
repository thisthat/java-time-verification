package IntermediateModel.structure;

import java.util.ArrayList;
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

	List<IASTMethod> methods = new ArrayList<>();
	String name;
	Visibility accessRight;
	List<String> implmentsInterfaces;
	String extendClass;

	public ASTClass(String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces){
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public ASTClass(String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces, List<IASTMethod> methods) {
		this.methods = methods;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public List<? extends IASTMethod> getMethods() {
		return methods;
	}

	public String getName() {
		return name;
	}

	public Visibility getAccessRight() {
		return accessRight;
	}

	public List<String> getImplmentsInterfaces() {
		return implmentsInterfaces;
	}

	public String getExtendClass() {
		return extendClass;
	}

	public void addMethod(IASTMethod method){
		methods.add(method);
	}

	public String toString(){
		String out;
		out = name + "\n";
		for(IASTMethod m : methods){
			out += m.toString();
		}
		return out;
	}

}
