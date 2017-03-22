package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTMethodCall extends IASTStm implements IASTRE {

	private String methodName;
	private IASTRE exprCallee;
	List<IASTRE> parameters;
	String classPointed = null;

	public ASTMethodCall(int start, int end, String methodName, IASTRE exprCallee) {
		super(start, end);
		this.methodName = methodName;
		this.exprCallee = exprCallee;
	}


	public ASTMethodCall(int start, int end, String methodName, IASTRE exprCallee, List<IASTRE> parameters) {
		super(start, end);
		this.methodName = methodName;
		this.exprCallee = exprCallee;
		this.parameters = parameters;
	}

	public IASTRE getExprCallee() {
		return exprCallee;
	}

	public void setExprCallee(IASTRE exprCallee) {
		this.exprCallee = exprCallee;
	}

	public List<IASTRE> getParameters() {
		return parameters;
	}

	public void setParameters(List<IASTRE> parameters) {
		this.parameters = parameters;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public String toString() {
		return "ASTMethodCall{" +
				"methodName='" + methodName + '\'' +
				", exprCallee=" + exprCallee +
				", parameters=" + parameters +
				'}';
	}

	public String getClassPointed() {
		return classPointed;
	}

	public void setClassPointed(String classPointed) {
		this.classPointed = classPointed;
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTMethodCall(this);
		if(exprCallee != null)
			exprCallee.visit(visitor);
		for(IASTRE p : parameters){
			p.visit(visitor);
		}
		visitor.exitASTMethodCall(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTMethodCall(this);
		if(exprCallee != null)
			exprCallee.visit(visitor);
		for(IASTRE p : parameters){
			p.visit(visitor);
		}
		visitor.exitASTMethodCall(this);
		visitor.exitAll(this);
	}
}