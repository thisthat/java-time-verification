package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.interfaces.IASTVisitor;
import org.antlr.v4.runtime.Token;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTAttribute extends IASTStm implements IASTVar, IASTVisitor {
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

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTAttribute(this);
		if(expr != null)
			expr.visit(visitor);
		visitor.exitASTAttribute(this);
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ASTVariable)
			return equals((ASTVariable) o);
		if (this == o) return true;
		if (!(o instanceof ASTAttribute)) return false;

		ASTAttribute that = (ASTAttribute) o;

		if (getAccessRight() != that.getAccessRight()) return false;
		if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
		return getExpr() != null ? getExpr().equals(that.getExpr()) : that.getExpr() == null;
	}

	public boolean equals(ASTVariable o) {
		return this.getName() == o.getName() && this.getType() == o.getType();
	}


}
