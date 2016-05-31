package IntermediateModel.structure.expression;


import IntermediateModel.interfaces.IASTStm;
import com.google.common.annotations.Beta;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
@Beta
public class ASTVar extends IASTStm {
	protected ASTVar(int start, int end) {
		super(start, end);
	}
}
