package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTExpression;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTRE extends IASTStm {

	public ASTRE(Token start, Token end) {
		super(start, end);
	}

	@Override
	public String toString() {
		return "\t\t->" + code;
	}
}
