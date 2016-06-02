package IntermediateModel.structure.expression.binary;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.expression.ASTBinary;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTGreater extends ASTBinary {


	public ASTGreater(Token start, Token end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end, left, right, op);
	}

	public ASTGreater(int start, int end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end, left, right, op);
	}

	@Override
	public String toString() {
		return "ASTGreater{" +
				"left=" + left +
				", right=" + right +
				", op=" + op +
				'}';
	}
}
