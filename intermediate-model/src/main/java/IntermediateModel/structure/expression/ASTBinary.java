package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTExpression;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTRE;
import org.antlr.v4.runtime.Token;
import com.google.common.annotations.Beta;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
@Beta
public class ASTBinary extends IASTStm implements IASTExpression {

	IASTExpression.Type operator;
	ASTRE left;
	ASTRE right;

	protected ASTBinary(Token start, Token end, IASTExpression.Type operator) {
		super(start, end);
		this.operator = operator;
	}
	protected ASTBinary(Token start, Token end, IASTExpression.Type operator, ASTRE left, ASTRE right) {
		super(start, end);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public IASTExpression.Type getOperator() {
		return operator;
	}

	public ASTRE getLeft() {
		return left;
	}

	public void setLeft(ASTRE left) {
		this.left = left;
	}

	public ASTRE getRight() {
		return right;
	}

	public void setRight(ASTRE right) {
		this.right = right;
	}
}
