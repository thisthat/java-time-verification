package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.expression.*;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class REParser {

	public static IASTRE getExpr(ParserRuleContext ctx){
		return new NotYetImplemented(ctx.start, ctx.stop);
	}
}
