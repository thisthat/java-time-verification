package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.expression.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class REParserJDT {

	public static IASTRE getExpr(ASTNode node){
		if(node == null){
			return null;
		}
		if(node instanceof ExpressionStatement){
			return handleExpressionStatement((ExpressionStatement)node);
		} else {
			return handleExpression((Expression) node);
		}

	}

	private static IASTRE handleExpressionStatement(ExpressionStatement exprStm){
		return handleExpression(exprStm.getExpression());
	}

	private static IASTRE handleExpression(Expression expr){
		if(expr instanceof SimpleName){
			return simpleName((SimpleName) expr);
		} else if(expr instanceof MethodInvocation){
			return methodInvocation((MethodInvocation)expr);
		} else if(expr instanceof PostfixExpression){
			return postFix((PostfixExpression)expr);
		} else if(expr instanceof Assignment){
			return assignment((Assignment)expr);
		} else if(expr instanceof StringLiteral){
			return literal((StringLiteral) expr );
		} else if(expr instanceof BooleanLiteral){
			return literal((BooleanLiteral) expr );
		} else if(expr instanceof NullLiteral){
			return literal((NullLiteral) expr );
		} else if(expr instanceof ClassInstanceCreation){
			return newObject((ClassInstanceCreation)expr);
		}

		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new NotYetImplemented(start,stop);
	}

	private static IASTRE newObject(ClassInstanceCreation expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		List<IASTRE> pars = new ArrayList<>();
		for(Object p : expr.arguments()){
			pars.add(
					getExpr((ASTNode) p)
			);
		}
		return new ASTNewObject(start,stop, type, pars);
	}

	private static IASTRE literal(NullLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private static IASTRE literal(BooleanLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private static IASTRE literal(StringLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.getEscapedValue());
	}

	private static IASTRE assignment(Assignment expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.OPERATOR op = getOperator(expr.getOperator().toString());
		IASTRE l = getExpr(expr.getLeftHandSide());
		IASTRE r = getExpr(expr.getRightHandSide());
		return new ASTAssignment(start, stop, l, r, op );
	}

	private static IASTRE postFix(PostfixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.ADDDEC op = IASTRE.ADDDEC.increment;
		if(expr.getOperator().equals("--")){
			op = IASTRE.ADDDEC.decrement;
		}
		IASTRE e = getExpr(expr.getOperand());
		return new ASTPostOp(start, stop, e, op);
	}

	private static IASTRE simpleName(SimpleName expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.getIdentifier());
	}

	private static IASTRE methodInvocation(MethodInvocation node) {
		int start, stop;
		start = node.getStartPosition();
		stop = start + node.getLength();
		String name = node.getName().getFullyQualifiedName();
		IASTRE exprCallee = getExpr(node.getExpression());
		List<IASTRE> pars = new ArrayList<>();
		for(Object p : node.arguments()){
			pars.add(
					getExpr( (ASTNode) p )
			);
		}
		return new ASTMethodCall(start,stop, name, exprCallee, pars );
	}

	public static IASTRE.OPERATOR getOperator(String op){
		switch (op){
			case "=": return IASTRE.OPERATOR.equal;
		}
		return IASTRE.OPERATOR.equal;
	}

}
