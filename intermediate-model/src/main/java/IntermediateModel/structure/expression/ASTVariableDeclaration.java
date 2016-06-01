package IntermediateModel.structure.expression;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTVariableDeclaration extends IASTStm implements IASTRE {

	String type;
	IASTRE name;
	IASTRE expr;

	public ASTVariableDeclaration(Token start, Token end, String type, IASTRE name, IASTRE expr) {
		super(start, end);
		this.type = type;
		this.name = name;
		this.expr = expr;
	}

	public ASTVariableDeclaration(int start, int end, String type, IASTRE name, IASTRE expr) {
		super(start, end);
		this.type = type;
		this.name = name;
		this.expr = expr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public IASTRE getName() {
		return name;
	}

	public void setName(IASTRE name) {
		this.name = name;
	}

	public IASTRE getExpr() {
		return expr;
	}

	public void setExpr(IASTRE expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "ASTVariableDeclaration{" +
				"type='" + type + '\'' +
				", name='" + name + '\'' +
				", expr=" + expr +
				'}';
	}
}
