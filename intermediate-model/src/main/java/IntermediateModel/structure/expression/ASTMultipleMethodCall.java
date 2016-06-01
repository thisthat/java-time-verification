package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTMultipleMethodCall extends IASTStm implements IASTRE {

	List<IASTRE> methods = new ArrayList<>();
	IASTRE variable;

	public ASTMultipleMethodCall(Token start, Token end, List<IASTRE> methods) {
		super(start, end);
		this.variable = variable;
		this.methods = methods;
		setVariable();
	}

	public ASTMultipleMethodCall(int start, int end, List<IASTRE> methods) {
		super(start, end);
		this.variable = variable;
		this.methods = methods;
		setVariable();
	}

	private void setVariable(){
		for(IASTRE m : methods){
			if(m instanceof ASTMethodCall && ((ASTMethodCall)m).getExprCallee() != null) {
				variable = ((ASTMethodCall)m).getExprCallee();
			}
		}
	}

	@Override
	public String toString() {
		return "ASTMultipleMethodCall{" +
				"methods=" + methods +
				", variable=" + variable +
				'}';
	}
}
