package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTRE extends IASTStm implements IASTVisitor {

	IASTRE expression;

	public ASTRE(Token start, Token end, IASTRE expression) {
		super(start, end);
		this.expression = expression;
	}
	public ASTRE(int start, int end, IASTRE expression) {
		super(start, end);
		this.expression = expression;
	}

	public String getExpressionName(){
		return expression.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		if(expression == null)
			return "::RE NULL::";
		return "::::REXP:::" + expression.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}

	public IASTRE getExpression() {
		return expression;
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTRE(this);
		expression.visit(visitor);
	}
}
