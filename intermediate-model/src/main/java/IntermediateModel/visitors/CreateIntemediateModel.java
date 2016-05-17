package IntermediateModel.visitors;



import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.*;
import IntermediateModel.visitors.utility.Getter;
import XALConversion.util.parsing.Exists;
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
		//don't do twice
		if(indexBody > 0) ctx.children.remove(indexBody);
		if(indexUpdate > 0) ctx.children.remove(indexUpdate);
		if(indexCheck > 0) ctx.children.remove(indexCheck);
		if(indexInit > 0) ctx.children.remove(indexInit);
	}
	@Override
	public void enterEnhancedForStatement(@NotNull EnhancedForStatementContext ctx) {
		int indexType = 2, indexVar = 3, indexExpr = 5, indexBody = 7;
		IASTHasStms bck = lastMethod;

		ASTVariable v = new ASTVariable(
				((ParserRuleContext)ctx.getChild(indexType)).start,
				((ParserRuleContext)ctx.getChild(indexVar)).stop,
				ctx.getChild(indexVar).getText(),
				ctx.getChild(indexType).getText()
		);

		ASTRE expr = getExprState((ParserRuleContext) ctx.getChild(indexExpr));

		ASTForEach foreach = new ASTForEach(ctx.start, ctx.stop, v, expr);
		lastMethod.addStms(foreach);
		lastMethod = foreach;
		//body
		if(Exists.Block((ParserRuleContext)ctx.getChild(indexBody))){
			walk(this, ctx.getChild(indexBody));
		} else {
			generateState((ParserRuleContext) ctx.getChild(indexBody));
		}
		//return to the begining
		lastMethod = bck;
		//delete expressions
		ctx.children.remove(indexBody);
		ctx.children.remove(indexExpr);
		ctx.children.remove(indexVar);
	}

	@Override
	public void enterIfThenStatement(@NotNull IfThenStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexGuard = 2, indexIf = 4;

		//Expression before
		ASTRE guard = getExprState((ParserRuleContext) ctx.getChild(indexGuard));
		ASTIf ifstm = new ASTIf(ctx.start, ctx.stop, guard);
		lastMethod.addStms(ifstm);

		//IF Branch
		ASTIf.ASTIfStms ifBranch = ifstm.new ASTIfStms( ((ParserRuleContext)ctx.getChild(indexIf)).start, ((ParserRuleContext)ctx.getChild(indexIf)).stop);
		ifstm.setIfBranch(ifBranch);
		lastMethod = ifBranch;
		produceIfElseCode(ctx.getChild(indexIf));


		lastMethod = bck;
		//don't visit them anymore
		ctx.children.remove(indexIf);
		ctx.children.remove(indexGuard);

	}

	@Override
	public void enterIfThenElseStatement(@NotNull IfThenElseStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexGuard = 2, indexIf = 4, indexElse = 6;
		//Expression before
		ASTRE guard = getExprState((ParserRuleContext) ctx.getChild(indexGuard));
		ASTIf ifstm = new ASTIf(ctx.start, ctx.stop, guard);
		lastMethod.addStms(ifstm);

		//IF BRANCH
		ASTIf.ASTIfStms ifBranch = ifstm.new ASTIfStms( ((ParserRuleContext)ctx.getChild(indexIf)).start, ((ParserRuleContext)ctx.getChild(indexIf)).stop);
		ifstm.setIfBranch(ifBranch);
		lastMethod = ifBranch;
		produceIfElseCode(ctx.getChild(indexIf));

		//ELSE BRANCH
		ASTIf.ASTElseStms elseBranch = ifstm.new ASTElseStms( ((ParserRuleContext)ctx.getChild(indexElse)).start, ((ParserRuleContext)ctx.getChild(indexElse)).stop);
		ifstm.setElseBranch(elseBranch);
		lastMethod = elseBranch;
		produceIfElseCode(ctx.getChild(indexElse));

		lastMethod = bck;
		//don't visit them anymore
		ctx.children.remove(indexElse);
		ctx.children.remove(indexIf);
		ctx.children.remove(indexGuard);

	}

	private void produceIfElseCode(ParseTree ctx){
		if(ctx instanceof StatementContext && !ctx.getText().startsWith("{")){
			//problem if we have inside another construct, like if/while/for/..
			//we have to check if there are inside such construct
			if(Exists.Has2Walk((ParserRuleContext) ctx)){
				walk(this, ctx);
			}
			else {
				generateState((ParserRuleContext) ctx);
			}
		}
		else if(ctx.getText().startsWith("{") &&
				(
					ctx instanceof StatementContext ||
					ctx instanceof StatementNoShortIfContext
				)
				){
			walk(this, ctx.getChild(0));
		}
		else {
			walk(this,ctx);
		}
	}


	@Override
	public void enterTryStatement(@NotNull TryStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexTry = 1, indexCatch = 2, indexFinally = 3;

		ASTTry trystm = new ASTTry(ctx.start, ctx.stop);
		lastMethod.addStms(trystm);

		ASTTry.ASTTryBranch tryBranch = trystm.new ASTTryBranch(
				((ParserRuleContext)ctx.getChild(indexTry)).start,
				((ParserRuleContext)ctx.getChild(indexTry)).stop
		);
		trystm.setTryBranch(tryBranch);
		lastMethod = tryBranch;
		//body
		walk((ParserRuleContext) ctx.getChild(indexTry));

		//Catch Block
		if(ctx.getChild(indexCatch) instanceof CatchesContext){
			CatchesContext ctch = (CatchesContext) ctx.getChild(indexCatch);
			int nCatches = ctch.children.size();
			for(int i = 0; i < nCatches; i++){
				ASTTry.ASTCatchBranch catchBranch = trystm.new ASTCatchBranch(
						((ParserRuleContext)ctch.getChild(i)).start,
						((ParserRuleContext)ctch.getChild(i)).stop,
						Getter.catchClausole((ParserRuleContext)ctch.getChild(i))
				);
				trystm.addCatchBranch(catchBranch);
				lastMethod = catchBranch;
				walk((ParserRuleContext) ctch.getChild(i));
			}
		} else {
			indexCatch = -1;
			indexFinally--;
		}

		if(ctx.getChild(indexFinally) instanceof Finally_Context){
			ASTTry.ASTFinallyBranch finallyBranch = trystm.new ASTFinallyBranch(
					((ParserRuleContext) ctx.getChild(indexFinally)).start,
					((ParserRuleContext) ctx.getChild(indexFinally)).stop
			);
			trystm.setFinallyBranch(finallyBranch);
			lastMethod = finallyBranch;
			walk((ParserRuleContext) ctx.getChild(indexFinally));
		} else {
			indexFinally = -1;
		}

		lastMethod = bck;
		if(indexFinally > 0) ctx.children.remove(indexFinally);
		if(indexCatch > 0) ctx.children.remove(indexCatch);
		if(indexTry > 0) ctx.children.remove(indexTry);
		super.enterTryStatement(ctx);
	}

	@Override
	public void enterContinueStatement(@NotNull ContinueStatementContext ctx) {
		generateState(ctx);
	}

	@Override
	public void enterBlockStatement(@NotNull BlockStatementContext ctx) {
		generateState(ctx);
	}

	public void enterWhileStatement(@NotNull WhileStatementContext ctx) {
		int indexExp = 2, indexBody = 4;
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

	protected void walk(ParserRuleContext ctx){
		if(Exists.Block(ctx)){
			walk(this, ctx);
		} else {
			generateState((ParserRuleContext) ctx);
		}
	}
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
