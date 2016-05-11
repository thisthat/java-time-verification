package IntermediateModel.visitors.utility;

import IntermediateModel.structure.ASTClass;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
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
		ASTClass.Visibility visibility = ASTClass.Visibility.PUBLIC;
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
}
