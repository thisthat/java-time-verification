package IntermediateModelHelper.indexing.structure;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * The following class is used to save some data in a MongoDB.
 * The data stored consists in:
 * <ul>
 *     <li>Package name where it belongs to</li>
 *     <li>Class Name where it belongs to</li>
 *     <li>Method where it belongs to</li>
 *     <li>Expression where it synchronized to</li>
 *     <li>Position in the code</li>
 * </ul>
 * The class is just a <i>Struct</i> that serves the purpose of storing information only.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexSyncBlock {
	String packageName = "";
	String className = "";
	String methodName = "";
	List<String> signature = new ArrayList<>();
	String expr = null;
	int start = 0;
	int end = 0;
	int line = 0;
	IndexEnv env;
	boolean isAccessibleFromOutside = false;

	public IndexSyncBlock() {
	}

	public IndexSyncBlock(IndexSyncBlock s) {
		this.packageName = s.getPackageName();
		this.className = s.getClassName();
		this.methodName = s.getMethodName();
		this.expr = s.getExpr();
		this.start = s.getStart();
		this.end = s.getEnd();
		this.line = s.getLine();
		this.env = s.getEnv();
		this.signature = s.getSignature();
	}


	public IndexSyncBlock(String packageName, String className, String methodName, String expr, int start, int end, int line, IndexEnv env, boolean isAccessibleFromOutside, List<String> signature) {
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.expr = expr;
		this.start = start;
		this.end = end;
		this.line = line;
		this.env = env;
		this.isAccessibleFromOutside = isAccessibleFromOutside;
		this.signature = signature;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public IndexEnv getEnv() {
		return env;
	}

	public void setEnv(IndexEnv env) {
		this.env = env;
	}

	public boolean isAccessibleFromOutside() {
		return isAccessibleFromOutside;
	}

	public void setAccessibleFromOutside(boolean accessibleFromOutside) {
		isAccessibleFromOutside = accessibleFromOutside;
	}

	public List<String> getSignature() {
		return signature;
	}

	public void setSignature(List<String> signature) {
		this.signature = signature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IndexSyncBlock)) return false;

		IndexSyncBlock that = (IndexSyncBlock) o;

		if (getStart() != that.getStart()) return false;
		if (getEnd() != that.getEnd()) return false;
		if (getLine() != that.getLine()) return false;
		if (getPackageName() != null ? !getPackageName().equals(that.getPackageName()) : that.getPackageName() != null)
			return false;
		if (getClassName() != null ? !getClassName().equals(that.getClassName()) : that.getClassName() != null)
			return false;
		if (getMethodName() != null ? !getMethodName().equals(that.getMethodName()) : that.getMethodName() != null)
			return false;
		return getExpr() != null ? getExpr().equals(that.getExpr()) : that.getExpr() == null;

	}

	@Override
	public int hashCode() {
		int result = getPackageName() != null ? getPackageName().hashCode() : 0;
		result = 31 * result + (getClassName() != null ? getClassName().hashCode() : 0);
		result = 31 * result + (getMethodName() != null ? getMethodName().hashCode() : 0);
		result = 31 * result + (getExpr() != null ? getExpr().hashCode() : 0);
		result = 31 * result + getStart();
		result = 31 * result + getEnd();
		result = 31 * result + getLine();
		return result;
	}

	public boolean isInMethod(IASTMethod method) {
		if(!this.methodName.equals(method.getName())) return false;
		if(this.signature.size() != method.getParameters().size()) return false;
		boolean flag = true;
		for(int i = 0; i < this.signature.size(); i++){
			if(!signature.get(i).equals(method.getParameters().get(i).getType())){
				flag = false;
			}
		}
		return flag;
	}
}
