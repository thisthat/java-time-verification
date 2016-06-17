package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.ASTVariable;
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
		} else if(expr instanceof PrefixExpression){
			return preFix((PrefixExpression)expr);
		} else if(expr instanceof Assignment){
			return assignment((Assignment)expr);
		} else if(expr instanceof StringLiteral){
			return literal((StringLiteral) expr );
		} else if(expr instanceof BooleanLiteral){
			return literal((BooleanLiteral) expr );
		} else if(expr instanceof NumberLiteral){
			return literal((NumberLiteral) expr );
		} else if(expr instanceof ThisExpression){
			return literal((ThisExpression) expr );
		} else if(expr instanceof NullLiteral){
			return literal((NullLiteral) expr );
		} else if(expr instanceof TypeLiteral){
			return literal((TypeLiteral) expr );
		} else if(expr instanceof ClassInstanceCreation){
			return newObject((ClassInstanceCreation)expr);
		} else if(expr instanceof InfixExpression){
			return mathExpression((InfixExpression) expr);
		} else if(expr instanceof FieldAccess){
			return fieldAccess((FieldAccess) expr);
		} else if(expr instanceof QualifiedName){
			return fieldAccess((QualifiedName) expr);
		} else if(expr instanceof VariableDeclarationExpression) {
			return variableDeclaration((VariableDeclarationExpression)expr);
		} else if(expr instanceof ParenthesizedExpression){
			return getExpr(((ParenthesizedExpression) expr).getExpression());
		} else if(expr instanceof CastExpression){
			return cast((CastExpression) expr);
		}

		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new NotYetImplemented(start,stop);
	}

	private static IASTRE cast(CastExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		IASTRE e = getExpr(expr.getExpression());
		return new ASTCast(start,stop, type, e);
	}

	private static IASTRE variableDeclaration(VariableDeclarationExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		IASTRE ret = null;
		if(expr.fragments().size() > 1){
			//multiple definition
			List<IASTRE> vars = new ArrayList<>();
			for(Object o :expr.fragments()){
				VariableDeclarationFragment subVar = (VariableDeclarationFragment) o;
				int vStart, vStop;
				vStart = subVar.getStartPosition();
				vStop = vStart + subVar.getLength();
				IASTRE name = getExpr(subVar.getName());
				IASTRE e = getExpr(subVar.getInitializer());
				ASTVariableDeclaration v = new ASTVariableDeclaration(vStart, vStop, type, name, e);
				vars.add(v);
			}
			ASTVariableMultipleDeclaration multiplevars = new ASTVariableMultipleDeclaration(start,stop,type, vars);
			ret = multiplevars;
		} else {
			for(Object o :expr.fragments()){
				VariableDeclarationFragment subVar = (VariableDeclarationFragment) o;
				int vStart, vStop;
				vStart = subVar.getStartPosition();
				vStop = vStart + subVar.getLength();
				IASTRE name = getExpr(subVar.getName());
				IASTRE e = getExpr(subVar.getInitializer());
				ASTVariableDeclaration v = new ASTVariableDeclaration(vStart, vStop, type, name, e);
				ret = v;
			}

		}
		return ret;
	}

	private static IASTRE fieldAccess(FieldAccess expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE where;
		String who;
		where = getExpr(expr.getExpression());
		who = expr.getName().getIdentifier();
		return new ASTAttributeAccess(start,stop, where, who);
	}

	private static IASTRE fieldAccess(QualifiedName expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE where;
		String who;
		where = getExpr(expr.getQualifier());
		who = expr.getName().getIdentifier();
		return new ASTAttributeAccess(start,stop, where, who);
	}



	private static IASTRE mathExpression(InfixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.OPERATOR op = getOperator(expr.getOperator().toString());
		IASTRE l = getExpr(expr.getLeftOperand());
		IASTRE r = getExpr(expr.getRightOperand());
		ASTBinary bin = new ASTBinary(start,stop, l, r, op);
		IASTRE prev = bin;
		for(Object o : expr.extendedOperands()){
			ASTNode extExpr = (ASTNode) o;
			IASTRE extended = getExpr(extExpr);
			int extstart, extstop;
			extstart = extExpr.getStartPosition();
			extstop = extstart + extExpr.getLength();
			ASTBinary ext = new ASTBinary(extstart, extstop, prev, extended, op);
			prev = ext;
		}
		return prev;
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

	private static IASTRE literal(TypeLiteral expr) {
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

	private static IASTRE literal(NumberLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private static IASTRE literal(ThisExpression expr) {
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
	private static IASTRE preFix(PrefixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.ADDDEC op = IASTRE.ADDDEC.increment;
		if(expr.getOperator().equals("--")){
			op = IASTRE.ADDDEC.decrement;
		}
		IASTRE e = getExpr(expr.getOperand());
		return new ASTPreOp(start, stop, e, op);
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
			case "+": return IASTRE.OPERATOR.plus;
			case "-": return IASTRE.OPERATOR.minus;
			case "/": return IASTRE.OPERATOR.div;
			case "*": return IASTRE.OPERATOR.mul;
			case ">": return IASTRE.OPERATOR.greater;
			case ">=": return IASTRE.OPERATOR.greaterEqual;
			case "<": return IASTRE.OPERATOR.less;
			case "<=": return IASTRE.OPERATOR.lessEqual;
			case "==": return IASTRE.OPERATOR.equality;
		}
		return IASTRE.OPERATOR.equal;
	}

}
