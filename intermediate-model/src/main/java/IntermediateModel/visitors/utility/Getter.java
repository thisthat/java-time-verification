package IntermediateModel.visitors.utility;

import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTVariable;
import IntermediateModel.structure.LocalSearch;
import com.sun.xml.internal.xsom.impl.Ref;
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
					pars.add(parameter((ParserRuleContext) cc));
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
		return new ASTVariable(name,type);
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
}
