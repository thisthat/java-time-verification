package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTContinue extends IASTStm {

	public ASTContinue(Token start, Token end) {
		super(start, end);
	}

	@Override
	public String toString() {
		return "---> CONTINUE <--- ";
	}
}
