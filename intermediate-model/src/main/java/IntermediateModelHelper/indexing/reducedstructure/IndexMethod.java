package IntermediateModelHelper.indexing.reducedstructure;

import intermediateModel.structure.ASTVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexMethod {
	String packageName = "";
	String name = "";
	List<ASTVariable> parameters = new ArrayList<>();
	List<String> exceptionsThrowed = new ArrayList<>();
	int start = 0;
	int end = 0;
	int line = 0;
	boolean isConstructor = false;
	boolean isSync = false;

	public IndexMethod(String packageName, String name, List<ASTVariable> parameters, List<String> exceptionsThrowed, int start, int end, int line, boolean isConstructor, boolean isSync) {
		this.packageName = packageName;
		this.name = name;
		this.parameters = parameters;
		this.exceptionsThrowed = exceptionsThrowed;
		this.start = start;
		this.end = end;
		this.line = line;
		this.isConstructor = isConstructor;
		this.isSync = isSync;
	}

	public IndexMethod() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ASTVariable> getParameters() {
		return parameters;
	}

	public void setParameters(List<ASTVariable> parameters) {
		this.parameters = parameters;
	}

	public List<String> getExceptionsThrowed() {
		return exceptionsThrowed;
	}

	public void setExceptionsThrowed(List<String> exceptionsThrowed) {
		this.exceptionsThrowed = exceptionsThrowed;
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

	public boolean isConstructor() {
		return isConstructor;
	}

	public void setConstructor(boolean constructor) {
		isConstructor = constructor;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean sync) {
		isSync = sync;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String toString() {
		return name;
	}
}
