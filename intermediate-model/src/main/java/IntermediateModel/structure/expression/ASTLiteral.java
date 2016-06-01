package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTLiteral extends IASTStm implements IASTRE {
	public String value;

	public ASTLiteral(Token start, Token end, String value) {
		super(start, end);
		this.value = value;
	}

	public ASTLiteral(int start, int end, String value) {
		super(start, end);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
