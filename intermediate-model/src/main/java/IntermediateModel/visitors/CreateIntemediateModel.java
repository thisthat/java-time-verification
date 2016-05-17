package IntermediateModel.visitors;



import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.*;
import IntermediateModel.visitors.utility.Getter;
import XALConversion.util.parsing.Exists;
import XALStructure.items.XALTransition;
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
	private IASTHasStms lastMethod;

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
			ASTClass c = new ASTClass(ctx.start, ctx.stop, name,vis,extendsName,_implments);
			listOfClasses.add(c);
			stckOfClasses.push(c);
			lastClass = c;
		}
	}

	@Override
	public void exitClassDeclaration(@NotNull ClassDeclarationContext ctx) {
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
		ASTMethod method = new ASTMethod(ctx.start, ctx.stop, methodName, returnType, pars);
		lastClass.addMethod(method);
		lastMethod = method;
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
		ASTConstructor method = new ASTConstructor(ctx.start, ctx.stop, methodName, pars);
		lastClass.addMethod(method);
		lastMethod = method;
	}

	@Override
	public void enterBasicForStatement(@NotNull BasicForStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexInit = 2, indexCheck = 4, indexBody = 8, indexUpdate = 6;
		ASTFor forstm = new ASTFor(ctx.start,ctx.stop);
		lastMethod.addStms(forstm);
		lastMethod = forstm;
		//Init Expression
		if(ctx.getChild(indexInit) instanceof ForInitContext){
			ASTRE init = getExprState((ParserRuleContext) ctx.getChild(indexInit));
			forstm.setInit(init);
		} else {
			indexInit = -1;
			indexCheck--;
			indexBody--;
			indexUpdate--;
		}
		//Checks
		if(ctx.getChild(indexCheck) instanceof ExpressionContext) {
			ASTRE expr = getExprState((ParserRuleContext) ctx.getChild(indexCheck));
			forstm.setExpr(expr);
		} else {
			indexCheck = -1;
			indexBody--;
			indexUpdate--;
		}
		//update
		if(ctx.getChild(indexUpdate) instanceof ForUpdateContext){
			ASTRE update = getExprState((ParserRuleContext) ctx.getChild(indexInit));
			forstm.setPost(update);
		} else {
			indexBody--;
			indexUpdate = -1;
		}
		//walk
		if(Exists.Block((ParserRuleContext)ctx.getChild(indexBody))){
			walk(this, ctx.getChild(indexBody));
		} else {
			generateState((ParserRuleContext) ctx.getChild(indexBody));
		}
		lastMethod = bck;
	}
	@Override
	public void enterEnhancedForStatement(@NotNull EnhancedForStatementContext ctx) {
	}

	@Override
	public void enterContinueStatement(@NotNull ContinueStatementContext ctx) {
		generateState(ctx);
	}

	@Override
	public void enterBlockStatement(@NotNull BlockStatementContext ctx) {
		generateState(ctx);
	}

	protected void generateState(ParserRuleContext ctx){
		//we have special rules for the if/while/...
		IASTStm stm = getState(ctx);
		if(stm != null)
			lastMethod.addStms(stm);
	}

	protected IASTStm getState(ParserRuleContext ctx){
		//we have special rules for the if/while/...
		if(Exists.Has2Walk(ctx)){
			return null;
		}
		IASTStm state = null;
		//get the type
		if(Exists.Return(ctx)){
			state = Getter.returnStm(ctx);
		}
		else if(Exists.Continue(ctx)){
			state = Getter.continueStm(ctx);
		}
		else {
			state = new ASTRE(ctx.start, ctx.stop);
		}
		return state;
	}

	protected ASTRE getExprState(ParserRuleContext ctx){
		return new ASTRE(ctx.start, ctx.stop);
	}


	/*protected void addState(ParserRuleContext ctx, IASTStm stm, IASTHasStms who){
		who.addStms(stm);
	}

	protected void addState(ParserRuleContext ctx, IASTStm stm){
		addState(ctx,stm, lastMethod);
	}*/

	protected void walk(ParseTreeListener listener, ParseTree t) {
		if(t instanceof ErrorNode) {
			listener.visitErrorNode((ErrorNode)t);
		} else if(t instanceof TerminalNode) {
			listener.visitTerminal((TerminalNode)t);
		} else {
			RuleNode r = (RuleNode)t;
			this.enterRule(listener, r);
			int n = r.getChildCount();
			for(int i = 0; i < n; ++i) {
				this.walk(listener, r.getChild(i));
			}
			this.exitRule(listener, r);
		}
	}
	protected void enterRule(ParseTreeListener listener, RuleNode r) {
		ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
		listener.enterEveryRule(ctx);
		ctx.enterRule(listener);
	}

	protected void exitRule(ParseTreeListener listener, RuleNode r) {
		ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
		ctx.exitRule(listener);
		listener.exitEveryRule(ctx);
	}
}
