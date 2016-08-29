package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class ASTClass extends IASTStm implements IASTVisitor {


	String packageName;
	List<IASTMethod> methods = new ArrayList<>();
	List<ASTStatic> staticInit = new ArrayList<>();
	String name;
	Visibility accessRight;
	List<String> implmentsInterfaces;
	String extendClass;
	List<ASTImport> imports = new ArrayList<>();
	List<ASTAttribute> attributes = new ArrayList<>();
	String path;

	public ASTClass(Token start, Token end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces){
		super(start,end);
		this.packageName = packageName;
		this.name = name;
		this.accessRight = accessRight;
		this.extendClass = extendClass == null ? "Object" : extendClass;
		this.implmentsInterfaces = implmentsInterfaces;
	}

	public ASTClass(Token start, Token end, String packageName, String name, Visibility accessRight, String extendClass, List<String> implmentsInterfaces, List<IASTMethod> methods) {
		super(start,end);
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

	public void setPath(String path) {
		this.path = path;
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

	public void addAttribute(ASTAttribute attribute) {
		this.attributes.add(attribute);
	}

	public List<ASTAttribute> getAttributes() {
		return attributes;
	}

	public List<ASTStatic> getStaticInit() {
		return staticInit;
	}

	public void addStaticInit(ASTStatic s){
		this.staticInit.add(s);
	}

	public void setStaticInit(List<ASTStatic> staticInit) {
		this.staticInit = staticInit;
	}

	public void setAttributes(List<ASTAttribute> attributes) {
		this.attributes = attributes;
	}

	public String toString(){
		String out = "";
		out = packageName + "." + name;// + "\n";
		//for(ASTImport imp : imports){
		//	out += imp.toString();
		//}
		//for(ASTAttribute a : attributes){
		//	out += a.toString();
		//}
		//for(IASTMethod m : methods){
		//	out += m.toString();
		//}
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

	public String getPath(){
		return path;
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

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTClass(this);
		for(IASTMethod m : methods){
			m.visit(visitor);
		}
		for(ASTImport i : imports){
			i.visit(visitor);
		}
		for(ASTAttribute a : attributes){
			a.visit(visitor);
		}
	}
}
