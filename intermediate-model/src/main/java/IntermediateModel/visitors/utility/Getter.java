package IntermediateModel.visitors.utility;

import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.*;
import com.sun.xml.internal.xsom.impl.Ref;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.grammar.Java8CommentSupportedParser.*;
import sun.tools.tree.ReturnStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
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
		LocalSearch t = new LocalSearch() {
			public UnannTypeContext get(ParserRuleContext elm){
				UnannTypeContext f = null;
				for(ParseTree c : elm.children){
					if(c instanceof UnannTypeContext){
						f = (UnannTypeContext) c;
					}
					else if(c instanceof TerminalNode){
						continue;
					}
					else {
						UnannTypeContext tmp = get((ParserRuleContext) c);
						if(tmp != null){
							f = tmp;
						}
					}
				}
				return f;
			}
		};
		return t.get(child).getText();
	}
	public static String variableName(ParserRuleContext child){
		LocalSearch t = new LocalSearch() {
			public VariableDeclaratorIdContext get(ParserRuleContext elm){
				VariableDeclaratorIdContext f = null;
				for(ParseTree c : elm.children){
					if(c instanceof VariableDeclaratorIdContext){
						f = (VariableDeclaratorIdContext) c;
					}
					else if(c instanceof TerminalNode){
						continue;
					}
					else {
						VariableDeclaratorIdContext tmp = get((ParserRuleContext) c);
						if(tmp != null){
							f = tmp;
						}
					}
				}
				return f;
			}
		};
		return t.get(child).getText();
	}

	public static ASTReturn returnStm(ParserRuleContext child){
		LocalSearch t = new LocalSearch() {
			public ReturnStatementContext get(ParserRuleContext elm){
				ReturnStatementContext f = (elm instanceof ReturnStatementContext) ? (ReturnStatementContext) elm : null;
				for(ParseTree c : elm.children){
					if(c instanceof ReturnStatementContext){
						f = (ReturnStatementContext) c;
					}
					else if(c instanceof TerminalNode){
						continue;
					}
					else {
						ReturnStatementContext tmp = get((ParserRuleContext) c);
						if(tmp != null){
							f = tmp;
						}
					}
				}
				return f;
			}
		};
		ReturnStatementContext ret = t.get(child);
		int indexRetExpr = 1;
		return new ASTReturn(ret.start, ret.stop, rightExpression((ParserRuleContext) ret.getChild(indexRetExpr)));
	}

	public static ASTRE rightExpression(ParserRuleContext child){
		return new ASTRE(child.start, child.stop);
	}

	public static IASTStm continueStm(ParserRuleContext ctx) {
		LocalSearch t = new LocalSearch() {
			public ContinueStatementContext get(ParserRuleContext elm){
				ContinueStatementContext f = (elm instanceof ContinueStatementContext) ? (ContinueStatementContext) elm : null;
				for(ParseTree c : elm.children){
					if(c instanceof ReturnStatementContext){
						f = (ContinueStatementContext) c;
					}
					else if(c instanceof TerminalNode){
						continue;
					}
					else {
						ContinueStatementContext tmp = get((ParserRuleContext) c);
						if(tmp != null){
							f = tmp;
						}
					}
				}
				return f;
			}
		};
		ContinueStatementContext c = t.get(ctx);
		return new ASTContinue(c.start, c.stop);
	}


	public static ASTRE expressionStm(ParserRuleContext ctx){
		return new ASTRE(ctx.start, ctx.stop);
	}
}
