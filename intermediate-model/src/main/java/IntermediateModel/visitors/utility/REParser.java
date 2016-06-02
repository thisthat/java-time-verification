package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.LocalSearch;
import IntermediateModel.structure.expression.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class REParser {

	public static IASTRE getExpr(ParserRuleContext ctx){
		ParserRuleContext elm = getExpressionNode(ctx);
		if(elm instanceof ClassInstanceCreationExpression_lfno_primaryContext){
			return getNewObject(elm);
		} else if (elm instanceof LiteralContext ||
				elm instanceof TypeNameContext ||
				elm instanceof VariableDeclaratorIdContext ||
				elm instanceof ExpressionNameContext ||
				elm instanceof TerminalNode) {
			return getLiteral(elm);
		} else if(elm instanceof MethodInvocationContext ||
				elm instanceof MethodInvocation_lfno_primaryContext ||
				elm instanceof  MethodInvocation_lf_primaryContext){
			return getMethodCall(elm);
		} else if(elm instanceof PrimaryNoNewArray_lfno_primaryContext ||
				elm instanceof FieldAccessContext){
			//attribute access
			return getAttributeAccess(elm);
		} else if(elm instanceof LocalVariableDeclarationContext){
			return getVariableDeclaration(elm);
		} else if(elm instanceof RelationalExpressionContext ||
				elm instanceof ShiftExpressionContext ||
				elm instanceof AdditiveExpressionContext ||
				elm instanceof EqualityExpressionContext){
			return getMathExpression(elm);
		} else if(elm instanceof AssignmentContext){
			return getAssignment(elm);
		} else if(elm instanceof PostIncrementExpressionContext){
			return getPostIncremental(elm);
		} else if(elm instanceof CastExpressionContext){
			return getCast(elm);
		} else if(elm instanceof UnaryExpressionNotPlusMinusContext ||
				elm instanceof UnaryExpressionContext){
			return getUnary(elm);
		}
		else if(elm instanceof PrimaryContext){
			//not well define yet
			return getMethodCallee(elm);
		}
		if(elm == null){
			if(ctx.getText().equals("this")){
				return getLiteral(ctx);
			}
		}
		return new NotYetImplemented(ctx.start, ctx.stop, ctx.getClass().getCanonicalName());
	}



	public static ParserRuleContext getExpressionNode(ParserRuleContext ctx){
		ParserRuleContext ret = null;
		//special case support
		if(ctx.children.size() == 2 &&
				(ctx.getChild(0) instanceof CommentContext || ctx.getChild(1) instanceof CommentContext)
				){
			if(ctx.getChild(0) instanceof CommentContext){
				return getExpressionNode((ParserRuleContext) ctx.getChild(1));
			} else {
				return getExpressionNode((ParserRuleContext) ctx.getChild(0));
			}
		}
		//init ret var
		if(ctx.children.size() > 1) {
			ret = ctx;
		} else {
			//special cases
			if(ctx instanceof TypeNameContext){
				ret = ctx;
			}
		}

		for(ParseTree c : ctx.children){
			if(c instanceof TerminalNode)
				continue;
			if(((ParserRuleContext)c).children.size() == 2 &&
				(c.getChild(0) instanceof CommentContext || c.getChild(1) instanceof CommentContext)
			) {
				if(c.getChild(0) instanceof CommentContext){
					return getExpressionNode((ParserRuleContext) c.getChild(1));
				} else {
					return getExpressionNode((ParserRuleContext) c.getChild(0));
				}
			}
			else if( ((ParserRuleContext)c).children.size() > 1 &&
				!c.getText().endsWith(";") //avoid "expr ;"
				)
			{
				ret = (ParserRuleContext) c;
			} else if (c instanceof LiteralContext || c instanceof TypeNameContext || c instanceof ExpressionNameContext){
				ret = (ParserRuleContext) c;
			} else {
				ret = getExpressionNode((ParserRuleContext) c);
			}
		}
		return ret;
	}

	private static IASTRE getNewObject(ParserRuleContext ctx){
		IASTRE newObj = new NotYetImplemented(ctx.start,ctx.stop,ctx.getClass().getTypeName());
		if(ctx instanceof ClassInstanceCreationExpression_lfno_primaryContext){
			int indexType = 1, indexPars = 3;
			//pars
			List<IASTRE> pars = new ArrayList<>();
			if(!(ctx.getChild(indexPars) instanceof TerminalNode)) {
				for (ParseTree c : ((ParserRuleContext) ctx.getChild(indexPars)).children) {
					if (c instanceof TerminalNode)
						continue;
					pars.add(getExpr((ParserRuleContext) c));
				}
			}
			//type
			String type = ctx.getChild(indexType).getText();
			if(ctx.getChild(2) instanceof TypeArgumentsOrDiamondContext){
				type += IASTStm.getSrcFromToken(
						((ParserRuleContext)ctx.getChild(2)).start,
						((ParserRuleContext)ctx.getChild(2)).stop
				);
			}
			newObj = new ASTNewObject(ctx.start, ctx.stop, type, pars);
		}
		return newObj;
	}


	private static IASTRE getLiteral(ParserRuleContext elm) {
		if(elm instanceof LiteralContext || elm instanceof TypeNameContext || elm instanceof VariableDeclaratorIdContext ||
				elm instanceof ExpressionNameContext){
			return new ASTLiteral(elm.start, elm.stop, elm.getText());
		}
		//special case for this.
		else if(elm.getText().startsWith("this")){
			return new ASTLiteral(elm.start, elm.stop, elm.getText());
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getTypeName());
	}

	private static IASTRE getMethodCall(ParserRuleContext elm) {
		if( elm instanceof MethodInvocationContext ||
			elm instanceof MethodInvocation_lfno_primaryContext
			){
			if(elm.getChild(0) instanceof MethodNameContext){
				int indexMethodName = 0, indexPars = 2;
				List<IASTRE> pars = new ArrayList<>();
				if (!(elm.getChild(indexPars) instanceof TerminalNode)) {
					for (ParseTree c : ((ParserRuleContext) elm.getChild(indexPars)).children) {
						if (c instanceof TerminalNode)
							continue;
						pars.add(getExpr((ParserRuleContext) c));
					}
				}
				//type
				IASTRE exprCallee = null;
				return new ASTMethodCall(elm.start, elm.stop, elm.getChild(indexMethodName).getText(), exprCallee, pars);
			} else {
				int indexVar = 0, indexMethodName = 2, indexPars = 4;
				List<IASTRE> pars = new ArrayList<>();
				if (!(elm.getChild(indexPars) instanceof TerminalNode)) {
					for (ParseTree c : ((ParserRuleContext) elm.getChild(indexPars)).children) {
						if (c instanceof TerminalNode)
							continue;
						pars.add(getExpr((ParserRuleContext) c));
					}
				}
				//type
				IASTRE exprCallee = getMethodCallee((ParserRuleContext) elm.getChild(indexVar));
				return new ASTMethodCall(elm.start, elm.stop, elm.getChild(indexMethodName).getText(), exprCallee, pars);
			}
		} else if (elm instanceof MethodInvocation_lf_primaryContext) {
			int indexMethodName = 1, indexPars = 3;
			List<IASTRE> pars = new ArrayList<>();
			if(!(elm.getChild(indexPars) instanceof TerminalNode)){
				for(ParseTree c : ((ParserRuleContext)elm.getChild(indexPars)).children){
					if(c instanceof TerminalNode)
						continue;
					pars.add( getExpr((ParserRuleContext) c) );
				}
			}
			//var not exists
			return new ASTMethodCall(elm.start, elm.stop, elm.getChild(indexMethodName).getText(), null , pars);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getTypeName());
	}

	private static IASTRE getMethodCallee(ParserRuleContext elm) {
		if(elm instanceof TypeNameContext) {
			return getLiteral(elm);
		} else if (elm instanceof PrimaryContext){
			//multiple call to methods
			TypeNameContext variable = LocalSearch.get((ParserRuleContext) elm.getChild(0), TypeNameContext.class);
			IASTRE varName = getLiteral(variable == null ? elm : variable);
			List<IASTRE> methods = getAllMethodCall(elm);
			return new ASTMultipleMethodCall(elm.start, elm.stop, methods);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static List<IASTRE> getAllMethodCall(ParserRuleContext elm) {
		List<IASTRE> list = new ArrayList<>();
		for(ParseTree c : elm.children){
			if(c instanceof MethodInvocationContext || c instanceof MethodInvocation_lf_primaryContext || c instanceof  MethodInvocation_lfno_primaryContext) {
				list.add( getMethodCall((ParserRuleContext) c) );
			} else if(c instanceof TerminalNode){
				continue;
			}
			else {
				List<IASTRE> tmp = getAllMethodCall((ParserRuleContext) c);
				list.addAll(tmp);
			}
		}
		return list;
	}

	private static IASTRE getAttributeAccess(ParserRuleContext elm) {
		if(elm.getText().equals("this")){
			return new ASTLiteral(elm.start, elm.stop, "this");
		}
		if(elm instanceof PrimaryNoNewArray_lfno_primaryContext ||
			elm instanceof FieldAccessContext){
			return new ASTAttributeAccess(elm.start, elm.stop, elm.getChild(0).getText(),elm.getChild(2).getText());
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getVariableDeclaration(ParserRuleContext elm) {
		if(elm instanceof LocalVariableDeclarationContext){
			String type = IASTStm.getSrcFromToken( ((ParserRuleContext)elm.getChild(0)).start, ((ParserRuleContext)elm.getChild(0)).stop);
			ParserRuleContext child = getExpressionNode(elm);
			if(child == null){
				return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
			} else {
				IASTRE name = getLiteral((ParserRuleContext) child.getChild(0));
				IASTRE expr = getExpr((ParserRuleContext) child.getChild(2));
				return new ASTVariableDeclaration(elm.start, elm.stop, type, name, expr);
			}
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getAssignment(ParserRuleContext elm) {
		if(elm instanceof AssignmentContext){
			IASTRE l = getExpr((ParserRuleContext) elm.getChild(0));
			IASTRE r = getExpr((ParserRuleContext) elm.getChild(2));
			IASTRE.OPERATOR op = IASTRE.OPERATOR.equal;
			return new ASTAssignment(elm.start, elm.stop, l, r, op);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getPostIncremental(ParserRuleContext elm) {
		if(elm instanceof PostIncrementExpressionContext){
			return new ASTPostInc(elm.start, elm.stop, getExpr((ParserRuleContext) elm.getChild(0)));
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getMathExpression(ParserRuleContext elm) {
		if(elm instanceof RelationalExpressionContext){
			IASTRE l = getExpr((ParserRuleContext) elm.getChild(0));
			IASTRE r = getExpr((ParserRuleContext) elm.getChild(2));
			IASTRE.OPERATOR op = IASTRE.OPERATOR.lessEqual;
			if(elm.getChild(1).getText().equals("<")){
				op = IASTRE.OPERATOR.less;
			}
			return new ASTBinary(elm.start, elm.stop, l, r, op);
		}
		if(elm instanceof ShiftExpressionContext){
			IASTRE l = getExpr((ParserRuleContext) elm.getChild(0));
			IASTRE r = getExpr((ParserRuleContext) elm.getChild(3));
			IASTRE.OPERATOR op = IASTRE.OPERATOR.lessEqual;
			if(elm.getChild(1).getText().equals("<")){
				op = IASTRE.OPERATOR.shiftLeft;
			}
			return new ASTBinary(elm.start, elm.stop, l, r, op);
		}
		if(elm instanceof AdditiveExpressionContext){
			IASTRE l = getExpr((ParserRuleContext) elm.getChild(0));
			IASTRE r = getExpr((ParserRuleContext) elm.getChild(2));
			IASTRE.OPERATOR op = IASTRE.OPERATOR.plus;
			return new ASTBinary(elm.start, elm.stop, l, r, op);
		}
		if(elm instanceof EqualityExpressionContext){
			IASTRE l = getExpr((ParserRuleContext) elm.getChild(0));
			IASTRE r = getExpr((ParserRuleContext) elm.getChild(2));
			IASTRE.OPERATOR op = IASTRE.OPERATOR.equality;
			return new ASTBinary(elm.start, elm.stop, l, r, op);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getCast(ParserRuleContext elm) {
		if(elm instanceof CastExpressionContext){
			String type = elm.getChild(1).getText();
			IASTRE expr = getExpr((ParserRuleContext) elm.getChild(3));
			return new ASTCast(elm.start, elm.stop, type, expr);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}

	private static IASTRE getUnary(ParserRuleContext elm) {
		if(elm instanceof UnaryExpressionNotPlusMinusContext){
			return new ASTUnary(elm.start, elm.stop, IASTRE.OPERATOR.not, getExpr((ParserRuleContext) elm.getChild(1)));
		}
		if(elm instanceof UnaryExpressionContext){
			IASTRE.OPERATOR op = IASTRE.OPERATOR.minus;
			if(elm.getChild(0).getText().equals("+")){
				op = IASTRE.OPERATOR.plus;
			}
			IASTRE expr = getExpr((ParserRuleContext) elm.getChild(1));
			return new ASTUnary(elm.start, elm.stop, op, expr);
		}
		return new NotYetImplemented(elm.start, elm.stop, elm.getClass().getCanonicalName());
	}



}
