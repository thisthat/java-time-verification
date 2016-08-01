package intermediateModel.structure;

import intermediateModel.interfaces.ASTVisitor;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVisitor;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTVariableDeclaration;
import intermediateModel.visitors.DefaultASTVisitor;
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
		if(expression instanceof ASTVariableDeclaration){
			final String[] var_name = new String[1];
			((ASTVariableDeclaration) expression).getName().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTLiteral(ASTLiteral elm) {
					var_name[0] = elm.getValue();
				}
			});
			return var_name[0];
		}
		if(expression instanceof ASTBinary){
			final String[] var_name = new String[1];
			expression.visit(new DefaultASTVisitor(){
				@Override
				public void enterASTLiteral(ASTLiteral elm) {
					var_name[0] = elm.getValue();
				}
			});
			return expression.getClass().getSimpleName() + "_" + var_name[0];
		}
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
