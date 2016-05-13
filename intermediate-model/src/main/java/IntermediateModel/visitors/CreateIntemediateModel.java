package IntermediateModel.visitors;



import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTConstructor;
import IntermediateModel.structure.ASTMethod;
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
	private Stack<ASTClass> stckOfClasses = new Stack<>();
	private ASTClass lastClass;

	@Override
	public void enterClassDeclaration(@NotNull ClassDeclarationContext ctx) {
		System.err.print("ENTER CLASS:");
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
			stckOfClasses.push(c);
			lastClass = c;
			System.err.print(name);
		}
		System.err.print("\n");
	}

	@Override
	public void exitClassDeclaration(@NotNull ClassDeclarationContext ctx) {
		System.err.println("Exit");
		if(stckOfClasses.size() > 0){
			ASTClass tmpClass = stckOfClasses.pop();
			//problem with multiple subclasses
			if(tmpClass.equals(lastClass) && stckOfClasses.size() > 0){
				lastClass = stckOfClasses.peek();
			}
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
		ASTMethod method = new ASTMethod(methodName, returnType, pars);
		lastClass.addMethod(method);
		super.enterMethodDeclaration(ctx);
	}

	@Override

	public void enterConstructorDeclaration(@NotNull ConstructorDeclarationContext ctx) {

		int indexAnnotation = 0, indexAccessRight = 0, indexHeader = 1;
		int indexName = 0;
		int indexPars = 2;
		ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
		String returnType = "void";
		String methodName = "";
		List<ASTVariable> pars = new ArrayList<ASTVariable>();
		if(ctx.getChild(indexAnnotation) instanceof ConstructorModifierContext && ctx.getChild(indexAnnotation).getChild(0) instanceof AnnotationContext){
			indexAccessRight++;
			indexHeader++;
		}
		if(ctx.getChild(indexAccessRight) instanceof ConstructorModifierContext){
			vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexAccessRight));
		} else {
			indexHeader--;
			indexAccessRight = -1;
		}
		if(ctx.getChild(indexHeader) instanceof ConstructorDeclaratorContext) {
			ParserRuleContext header = (ParserRuleContext) ctx.getChild(indexHeader);
			methodName = header.getChild(indexName).getText();
			pars = Getter.parameterList((ParserRuleContext) header.getChild(indexPars));
		}
		ASTConstructor method = new ASTConstructor(methodName, pars);
		lastClass.addMethod(method);
	}
}
