package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.ASTRE;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTMethodCall extends ASTNewObject implements IASTRE {

	private String varName;

	protected ASTMethodCall(Token start, Token end, String typeName, String varName) {
		super(start, end, typeName);
		this.varName = varName;
	}

	protected ASTMethodCall(int start, int end, String typeName, String varName) {
		super(start, end, typeName);
		this.varName = varName;
	}

	protected ASTMethodCall(Token start, Token end, String typeName, String varName, List<ASTRE> parameters) {
		super(start, end, typeName, parameters);
		this.varName = varName;
	}

	protected ASTMethodCall(int start, int end, String typeName, String varName, List<ASTRE> parameters) {
		super(start, end, typeName, parameters);
		this.varName = varName;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}
}