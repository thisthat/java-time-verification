package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTMethodCall extends IASTStm implements IASTRE {

	private String methodName;
	private IASTRE exprCallee;
	List<IASTRE> parameters;

	public ASTMethodCall(Token start, Token end, String methodName, IASTRE exprCallee) {
		super(start, end);
		this.methodName = methodName;
		this.exprCallee = exprCallee;
	}

	public ASTMethodCall(int start, int end, String methodName, IASTRE exprCallee) {
		super(start, end);
		this.methodName = methodName;
		this.exprCallee = exprCallee;
	}

	public ASTMethodCall(Token start, Token end, String methodName, IASTRE exprCallee, List<IASTRE> parameters) {
		super(start, end);
		this.methodName = methodName;
		this.exprCallee = exprCallee;
		this.parameters = parameters;
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
}