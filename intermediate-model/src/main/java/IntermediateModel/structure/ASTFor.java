package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTFor extends IASTStm {
	List<IASTStm> stms = new ArrayList<>();
	List<ASTRE> init = new ArrayList<>();
	ASTRE expr;
	List<ASTRE> post = new ArrayList<>();

	public ASTFor(Token start, Token end) {
		super(start, end);
	}
}
