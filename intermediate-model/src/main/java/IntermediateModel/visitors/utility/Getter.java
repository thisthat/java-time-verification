package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.interfaces.LocalSearch;
import IntermediateModel.structure.*;
import com.sun.xml.internal.xsom.impl.Ref;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 *
 * This class helps to extract the pieces of the intermediate model from the ANTLR4 AST.
 * Each method search for a particular piece and translate it in the IM representation.
 *
 */
public class Getter {
	public static ASTClass.Visibility accessRightClass(ParserRuleContext elm){
		String access = elm.getText();
		ASTClass.Visibility visibility = ASTClass.Visibility.PRIVATE;
		switch(access){
			case "public": 		visibility = ASTClass.Visibility.PUBLIC; break;
			case "protected": 	visibility = ASTClass.Visibility.PROTECT; break;
			case "private": 	visibility = ASTClass.Visibility.PRIVATE; break;
			case "abstract": 	visibility = ASTClass.Visibility.ABSTRACT; break;
			case "final": 		visibility = ASTClass.Visibility.FINAL; break;
			case "strictfp": 	visibility = ASTClass.Visibility.STRICTFP; break;
		}
		return visibility;
	}

	public static String extendClass(ParserRuleContext elm){
		return elm.getChild(1).getText();
	}

	public static List<String> listInterfaces(ParserRuleContext child) {
		List<String> interfaces = new ArrayList<String>();
		for (ParseTree elm: ((ParserRuleContext) child.getChild(1)).children) {
			if(elm instanceof InterfaceTypeContext){
				interfaces.add(elm.getText());
			}
		}
		return interfaces;
	}

	public static List<String> listThrows(ParserRuleContext child) {
		List<String> exceptions = new ArrayList<String>();
		for (ParseTree elm: ((ParserRuleContext) child.getChild(1)).children) {
			if(elm instanceof ExceptionTypeContext){
				exceptions.add(elm.getText());
			}
		}
		return exceptions;
	}

	public static List<ASTVariable> parameterList(ParserRuleContext child) {
		List<ASTVariable> pars = new ArrayList<>();
		for(ParseTree c : child.children){
			if(c instanceof FormalParameterListContext){
				for (ParseTree cc: ((FormalParameterListContext) c).children) {
					if(cc instanceof TerminalNode){
						continue;
					} else {
						pars.add(parameter((ParserRuleContext) cc));
					}
				}
			}
			else if(c instanceof FormalParametersContext){
				for (ParseTree cc: ((FormalParametersContext) c).children) {
					if(cc instanceof TerminalNode){
						continue;
					} else {
						pars.add(parameter((ParserRuleContext) cc));
					}
				}
			}
			else if(c instanceof LastFormalParameterContext){
				for (ParseTree cc: ((LastFormalParameterContext) c).children) {
					if(cc instanceof TerminalNode){
						continue;
					} else {
						pars.add(parameter((ParserRuleContext) cc));
					}
				}
			}
			else{
				continue;
			}
		}
		return pars;
	}

	public static ASTVariable parameter(ParserRuleContext child){
		String type = variableType(child);
		String name = variableName(child);
		return new ASTVariable(child.start, child.stop, name,type);
	}
	public static String variableType(ParserRuleContext child){
		UnannTypeContext var = LocalSearch.get(child, UnannTypeContext.class);
		return child.start.getInputStream().getText(new Interval(var.start.getStartIndex(), var.stop.getStopIndex()));
	}
	public static String variableName(ParserRuleContext child){
		return LocalSearch.get(child, VariableDeclaratorIdContext.class).getText();
	}

	public static ASTReturn returnStm(ParserRuleContext child){
		ReturnStatementContext ret = LocalSearch.get(child,ReturnStatementContext.class);
		int indexRetExpr = 1;
		if(ret.getChild(indexRetExpr) instanceof TerminalNode){
			return new ASTReturn(ret.start, ret.stop, null);
		}
		return new ASTReturn(ret.start, ret.stop, rightExpression((ParserRuleContext) ret.getChild(indexRetExpr)));
	}

	public static ASTRE rightExpression(ParserRuleContext child){
		return new ASTRE(child.start, child.stop, REParser.getExpr(child));
	}

	public static IASTStm continueStm(ParserRuleContext ctx) {
		ContinueStatementContext c = LocalSearch.get(ctx,ContinueStatementContext.class);
		return new ASTContinue(c.start, c.stop);
	}

	public static ASTVariable catchClausole(ParserRuleContext child) {
		CatchFormalParameterContext c = LocalSearch.get(child, CatchFormalParameterContext.class);
		int indexName = 1, indexType = 0;
		return new ASTVariable(c.start, c.stop, c.getChild(indexName).getText(), c.getChild(indexType).getText());
	}

	public static List<String> switchLabel(ParserRuleContext child) {
		SwitchLabelsContext labels = LocalSearch.get(child, SwitchLabelsContext.class);
		List<String> ret = new ArrayList<>();
		int indexLabel = 1;
		for(int i = 0; i < labels.children.size(); i++){
			SwitchLabelContext label = (SwitchLabelContext) labels.getChild(i);
			ret.add( label.getChild(indexLabel).getText() );
		}
		return ret;
	}

	public static ASTBreak breakStm(ParserRuleContext ctx) {
		BreakStatementContext brk = LocalSearch.get(ctx, BreakStatementContext.class);
		return new ASTBreak(brk.start, brk.stop);
	}

	public static List<ASTRE> tryResources(ParserRuleContext ctx) {
		ResourceListContext res = LocalSearch.get(ctx, ResourceListContext.class);
		List<ASTRE> ret = new ArrayList<>();
		for(int i = 0; i < res.children.size(); i++){
			if(res.getChild(i) instanceof ResourceContext){
				ret.add(
						rightExpression((ParserRuleContext) res.getChild(i))
				);
			}
		}
		return ret;
	}

	public static IASTStm throwsStm(ParserRuleContext ctx) {
		ThrowStatementContext th = LocalSearch.get(ctx, ThrowStatementContext.class);
		ExpressionContext exc = LocalSearch.get(th, ExpressionContext.class);
		ASTRE e = new ASTRE(exc.start, exc.stop, REParser.getExpr(exc));
		return new ASTThrow(th.start, th.stop, e);
	}


	public static String staticImport(ParserRuleContext child) {
		int max = child.children.size() - 1; //skip the ;
		String name = "";
		for(int i = 2; i < max; i++){
				name += child.getChild(i).getText();
		}
		return name;
	}

	public static String normalImport(ParserRuleContext child) {
		return child.getChild(1).getText();
	}

	public static ASTRE attributeExpression(ParserRuleContext child) {
		ParserRuleContext expr = LocalSearch.get(child, VariableInitializerContext.class);
		if(expr == null) return null;
		return rightExpression(expr);
	}
}
