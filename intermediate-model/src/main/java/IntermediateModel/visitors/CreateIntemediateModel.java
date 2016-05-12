package IntermediateModel.visitors;



import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTVariable;
import IntermediateModel.visitors.utility.Getter;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Visitor of the AST that will creates a XAL tree structure.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */
public class CreateIntemediateModel extends Java8CommentSupportedBaseListener {


	public List<ASTClass> listOfClasses = new ArrayList<ASTClass>();



	@Override
	public void enterClassDeclaration(@NotNull ClassDeclarationContext ctx) {
		int indexAccessRight, indexExtends, indexImplements;
		if(ctx.getChild(0) instanceof NormalClassDeclarationContext){
			indexAccessRight = 0;
			indexExtends = 3;
			indexImplements = 4;
			NormalClassDeclarationContext elm = (NormalClassDeclarationContext) ctx.getChild(0);
			ASTClass.Visibility vis = Getter.accessRightClass((ParserRuleContext) elm.getChild(indexAccessRight));
			String name = elm.getChild(2).getText();
			String extendsName = null;
			//extends
			if(elm.getChild(indexExtends) instanceof SuperclassContext){
				extendsName = Getter.extendClass((ParserRuleContext) elm.getChild(indexExtends));
			}
			else {
				indexExtends = -1;
				indexImplements--;
			}
			//instance
			List<String> _implments = new ArrayList<String>();
			if(elm.getChild(indexImplements) instanceof SuperinterfacesContext){
				_implments = Getter.listInterfaces( (ParserRuleContext) elm.getChild(indexImplements) );
			}
			ASTClass c = new ASTClass(name,vis,extendsName,_implments);
			listOfClasses.add(c);
		}
	}

	@Override
	public void enterMethodDeclaration(@NotNull MethodDeclarationContext ctx) {
		int indexAnnotation = 0, indexAccessRight = 0, indexHeader = 1;
		int indexReturn = 0;
		int indexPars = 1;
		ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
		String returnType = "void";
		String methodName = "";
		List<ASTVariable> pars = new ArrayList<ASTVariable>();
		if(ctx.getChild(indexAnnotation) instanceof MethodModifierContext && ctx.getChild(indexAnnotation).getChild(0) instanceof AnnotationContext){
			indexAccessRight++;
			indexHeader++;
		}
		if(ctx.getChild(indexAccessRight) instanceof MethodModifierContext){
			vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexAccessRight));
		} else {
			indexHeader--;
			indexAccessRight = -1;
		}
		if(ctx.getChild(indexHeader) instanceof MethodHeaderContext) {
			ParserRuleContext header = (ParserRuleContext) ctx.getChild(indexHeader);
			returnType = header.getChild(indexReturn).getText();
			methodName = header.getChild(indexPars).getChild(0).getText();
			pars = Getter.parameterList((ParserRuleContext) header.getChild(indexPars));
		}


		super.enterMethodDeclaration(ctx);
	}
}
