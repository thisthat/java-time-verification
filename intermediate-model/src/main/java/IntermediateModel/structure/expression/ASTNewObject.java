package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTNewObject extends IASTStm implements IASTRE {

	List<ASTRE> parameters;
	String typeName;

	protected ASTNewObject(Token start, Token end, String typeName) {
		super(start, end);
		this.typeName = typeName;
	}

	protected ASTNewObject(int start, int end, String typeName) {
		super(start, end);
		this.typeName = typeName;
	}

	protected ASTNewObject(Token start, Token end, String typeName, List<ASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.parameters = parameters;
	}

	protected ASTNewObject(int start, int end, String typeName, List<ASTRE> parameters) {
		super(start, end);
		this.typeName = typeName;
		this.parameters = parameters;
	}

	public List<ASTRE> getParameters() {
		return parameters;
	}

	public void setParameters(List<ASTRE> parameters) {
		this.parameters = parameters;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}

