package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTExpression;
import IntermediateModel.interfaces.IASTStm;
import com.google.common.annotations.Beta;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
@Beta
public class ASTMethodCall extends IASTStm implements IASTExpression{

	protected ASTMethodCall(Token start, Token end) {
		super(start, end);
	}
}

