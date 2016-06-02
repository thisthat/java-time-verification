package IntermediateModel.structure.expression.binary;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.expression.ASTBinary;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTLess extends ASTBinary {


	public ASTLess(Token start, Token end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end, left, right, op);
	}

	public ASTLess(int start, int end, IASTRE left, IASTRE right, OPERATOR op) {
		super(start, end, left, right, op);
	}

	@Override
	public String toString() {
		return "ASTLess{" +
				"left=" + left +
				", right=" + right +
				", op=" + op +
				'}';
	}
}
