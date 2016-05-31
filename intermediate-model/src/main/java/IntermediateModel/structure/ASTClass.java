package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class ASTClass extends IASTStm {

	public enum Visibility {
		PUBLIC,
		PROTECT,
		ABSTRACT,
		FINAL,
		PRIVATE,
		STRICTFP
	}
	String packageName;
	List<IASTMethod> methods = new ArrayList<>();
	String name;
	Visibility accessRight;
	List<String> implmentsInterfaces;
	String extendClass;
	List<ASTImport> imports = new ArrayList<>();

	public ASTClass(Token start, Token end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces){
		super(start.getStartIndex(),end.getStopIndex());
		this.packageName = packageName;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public ASTClass(Token start, Token end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces, List<IASTMethod> methods) {
		super(start.getStartIndex(),end.getStopIndex());
		this.packageName = packageName;
		this.methods = methods;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public ASTClass(int start, int end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces){
		super(start,end);
		this.packageName = packageName;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public ASTClass(int start, int end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces, List<IASTMethod> methods) {
		super(start,end);
		this.packageName = packageName;
		this.methods = methods;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public List<ASTImport> getImports() {
		return imports;
	}

	public void setImports(List<ASTImport> imports) {
		this.imports = imports;
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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAccessRight(Visibility accessRight) {
		this.accessRight = accessRight;
	}

	public void setImplmentsInterfaces(List<String> implmentsInterfaces) {
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public void setExtendClass(String extendClass) {
		this.extendClass = extendClass;
	}

	public void setMethods(List<IASTMethod> methods) {
		this.methods = methods;
	}

	public String toString(){
		String out;
		out = packageName + "." + name + "\n";
		for(ASTImport imp : imports){
			out += imp.toString();
		}
		for(IASTMethod m : methods){
			out += m.toString();
		}
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTClass)) return false;

		ASTClass astClass = (ASTClass) o;

		if (packageName != null ? !packageName.equals(astClass.packageName) : astClass.packageName != null)
			return false;
		if (getMethods() != null ? !getMethods().equals(astClass.getMethods()) : astClass.getMethods() != null)
			return false;
		if (getName() != null ? !getName().equals(astClass.getName()) : astClass.getName() != null) return false;
		if (getAccessRight() != astClass.getAccessRight()) return false;
		if (getImplmentsInterfaces() != null ? !getImplmentsInterfaces().equals(astClass.getImplmentsInterfaces()) : astClass.getImplmentsInterfaces() != null)
			return false;
		if (getExtendClass() != null ? !getExtendClass().equals(astClass.getExtendClass()) : astClass.getExtendClass() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = packageName != null ? packageName.hashCode() : 0;
		result = 31 * result + (getMethods() != null ? getMethods().hashCode() : 0);
		result = 31 * result + (getName() != null ? getName().hashCode() : 0);
		result = 31 * result + (getAccessRight() != null ? getAccessRight().hashCode() : 0);
		result = 31 * result + (getImplmentsInterfaces() != null ? getImplmentsInterfaces().hashCode() : 0);
		result = 31 * result + (getExtendClass() != null ? getExtendClass().hashCode() : 0);
		return result;
	}
}
