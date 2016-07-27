package IntermediateModelHelper.indexing.reducedstructure;


import intermediateModel.structure.ASTRE;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexSyncBlock {
	String packageName = "";
	String className = "";
	String methodName = "";
	ASTRE expr = null;
	int start = 0;
	int end = 0;
	int line = 0;

	public IndexSyncBlock() {
	}

	public IndexSyncBlock(String packageName, String className, String methodName, ASTRE expr, int start, int end, int line) {
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.expr = expr;
		this.start = start;
		this.end = end;
		this.line = line;
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

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
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
}
