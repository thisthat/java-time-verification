package intermediateModel.structure.expression;

import intermediateModel.interfaces.ASTREVisitor;
import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTVariableDeclaration extends IASTStm implements IASTRE {

	String type;
	IASTRE name;
	IASTRE expr;
	private String typePointed;

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
				"}\n";
	}


	public String getNameString() {
		if(name instanceof ASTLiteral)
			return ((ASTLiteral) name).getValue();
		return "--not yet define--";
	}

	@Override
	public void visit(ASTREVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTVariableDeclaration(this);
		if(expr != null)
			expr.visit(visitor);
		visitor.exitASTVariableDeclaration(this);
		visitor.exitAll(this);
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterAll(this);
		visitor.enterASTVariableDeclaration(this);
		if(expr != null)
			expr.visit(visitor);
		visitor.exitASTVariableDeclaration(this);
		visitor.exitAll(this);
	}

	public void setTypePointed(String typePointed) {
		this.typePointed = typePointed;
	}

	public String getTypePointed() {
		return typePointed;
	}
}
