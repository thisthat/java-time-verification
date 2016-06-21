package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.ASTREVisitor;
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
		visitor.enterAll(this);
		visitor.enterASTCast(this);
		expr.visit(visitor);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTCast(this);
		expr.visit(visitor);
	}


}
