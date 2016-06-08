package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.ASTREVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTCast extends IASTStm implements IASTRE {

	private String type;
	private IASTRE expr;

	public ASTCast(Token start, Token end, String type, IASTRE expr) {
		super(start, end);
		this.type = type;
		this.expr = expr;
	}

	public ASTCast(int start, int end, String type, IASTRE expr) {
		super(start, end);
		this.type = type;
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "ASTCast{" +
				"type='" + type + '\'' +
				", expr=" + expr +
				'}';
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterASTCast(this);
		expr.visit(visitor);
	}
}
