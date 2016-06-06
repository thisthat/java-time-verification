package IntermediateModel.structure;

import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.expression.ASTVariableDeclaration;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTAttribute extends IASTStm implements IASTVar {
	private Visibility accessRight;
	private String type;
	private String name;
	private ASTRE expr;

	public ASTAttribute(Token start, Token end, Visibility accessRight, String type, String name, ASTRE expr) {
		super(start, end);
		this.accessRight = accessRight;
		this.type = type;
		this.name = name;
		this.expr = expr;
	}

	public ASTAttribute(int start, int end, Visibility accessRight, String type, String name, ASTRE expr) {
		super(start, end);
		this.accessRight = accessRight;
		this.type = type;
		this.name = name;
		this.expr = expr;
	}

	public Visibility getAccessRight() {
		return accessRight;
	}

	public void setAccessRight(Visibility accessRight) {
		this.accessRight = accessRight;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ASTRE getExpr() {
		return expr;
	}

	public void setExpr(ASTRE expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		return "ASTAttribute{" +
				"accessRight=" + accessRight +
				", type='" + type + '\'' +
				", name='" + name + '\'' +
				", expr=" + expr +
				"}\n";
	}
}
