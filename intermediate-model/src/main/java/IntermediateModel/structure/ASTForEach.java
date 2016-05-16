package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTForEach extends IASTStm {
	public ASTForEach(Token start, Token end) {
		super(start, end);
	}
}
