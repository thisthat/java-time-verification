package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.structure.expression.ASTVariableDeclaration;
import intermediateModel.visitors.DefaultASTVisitor;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ASTRE extends IASTStm implements IASTVisitor {

	IASTRE expression;
	private static int _ID = 0;

	public ASTRE(int start, int end, IASTRE expression) {
		super(start, end);
		this.expression = expression;
	}

	public String getExpressionName(){
		if(expression instanceof ASTVariableDeclaration){
			final String[] var_name = new String[1];
			((ASTVariableDeclaration) expression).getName().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTLiteral(ASTLiteral elm) {
					var_name[0] = elm.getValue();
				}
			});
			return "Declaration_" + var_name[0];
		}
		if(expression instanceof ASTBinary){
			IASTRE.OPERATOR op = ((ASTBinary) expression).getOp();
			return op.toString();// + "_" + _ID++;
		}
		if(expression instanceof ASTMethodCall){
			return "call_to_" + ((ASTMethodCall) expression).getMethodName();
		}
		if(expression instanceof ASTLiteral){
			return ((ASTLiteral) expression).getValue();
		}
		return expression.getClass().getSimpleName();// + "_" + _ID++;
	}

	private String escape(String name){
		if(name == null) {
			return "";
		}
		name = name.replace("\"","");
		name = name.replace(" ","");
		return name;
	}

	@Override
	public String toString() {
		if(expression == null)
			return "::RE NULL::";
		return "::::REXP:::" + expression.toString();
	}

	public String toText() {
		if(expression == null)
			return expression.getCode();
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ASTRE)) return false;

		ASTRE astre = (ASTRE) o;

		return astre.getCode().equals(this.getCode());

	}

	@Override
	public int hashCode() {
		return getExpression() != null ? getExpression().hashCode() : 0;
	}

	public IASTRE getExpression() {
		return expression;
	}

	@Override
	public void visit(ASTVisitor visitor) {
		visitor.enterASTRE(this);
		expression.visit(visitor);
		visitor.exitASTRE(this);
	}
}
