package IntermediateModelHelper.indexing.structure;


import IntermediateModelHelper.envirorment.Env;

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
	String expr = null;
	int start = 0;
	int end = 0;
	int line = 0;
	Env env;

	public IndexSyncBlock() {
	}

	public IndexSyncBlock(String packageName, String className, String methodName, String expr, int start, int end, int line, Env env) {
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.expr = expr;
		this.start = start;
		this.end = end;
		this.line = line;
		this.env = env;
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

	public Env getEnv() {
		return env;
	}

	public void setEnv(Env env) {
		this.env = env;
	}
}
