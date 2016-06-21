package intermediateModel.visitors;



import parser.ASTSrc;
import intermediateModel.interfaces.IASTHasStms;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.utility.Getter;
import intermediateModel.visitors.utility.REParser;
import XALConversion.util.parsing.Exists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Visitor of the AST that will creates a XAL tree structure.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */
public class CreateIntemediateModel extends Java8CommentSupportedBaseListener {


	public List<ASTClass> listOfClasses = new ArrayList<ASTClass>();
	private ASTClass lastClass;
	private IASTHasStms lastMethod;
	private String packageName = "";
	private List<ASTImport> listOfImports = new ArrayList<>();

	@Override
	public void enterCompilationUnit(@NotNull CompilationUnitContext ctx) {
		//Set the source
		ASTSrc.getInstance().setSource(ctx.start.getInputStream().getText(
				new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex())
		).toCharArray());
		//remove all the comments
		walk(new Java8CommentSupportedBaseListener(){
			@Override
			public void enterComment(@NotNull CommentContext ctx) {
				for(int i = 0; i < ctx.parent.getChildCount(); i++){
					if(ctx.getChild(i) instanceof CommentContext){
						ctx.children.remove(i);
					}
				}
			}
		}, ctx);
	}

	@Override
	public void enterPackageDeclaration(@NotNull PackageDeclarationContext ctx) {
		super.enterPackageDeclaration(ctx);
		for(int i = 1; i < ctx.children.size() - 1; i++){
			if(ctx.getChild(i) instanceof TerminalNode)
				packageName += ctx.getChild(i).getText();
		}
	}

	@Override
	public void enterImportDeclaration(@NotNull ImportDeclarationContext ctx) {
		boolean isStatic = false;
		int indexStatic = 0;
		String packagename = "";
		if(ctx.getChild(indexStatic) instanceof StaticImportOnDemandDeclarationContext){
			isStatic = true;
			packagename = Getter.staticImport((ParserRuleContext) ctx.getChild(indexStatic));
		} else {
			packagename = Getter.normalImport((ParserRuleContext) ctx.getChild(0));
		}
		listOfImports.add(new ASTImport(ctx.start, ctx.stop, isStatic, packagename));
	}

	@Override
	public void enterClassDeclaration(@NotNull ClassDeclarationContext ctx) {
		ASTClass bck = lastClass;
		int indexAccessRight, indexName, indexExtends, indexImplements, indexBody;
		if(ctx.getChild(0) instanceof NormalClassDeclarationContext){
			indexAccessRight = 0;
			indexName = 2;
			indexExtends = 3;
			indexImplements = 4;
			indexBody = 5;
			NormalClassDeclarationContext elm = (NormalClassDeclarationContext) ctx.getChild(0);
			ASTClass.Visibility vis = Getter.accessRightClass((ParserRuleContext) elm.getChild(indexAccessRight));
			String name = elm.getChild(indexName).getText();
			String extendsName = null;
			//extends
			if(elm.getChild(indexExtends) instanceof SuperclassContext){
				extendsName = Getter.extendClass((ParserRuleContext) elm.getChild(indexExtends));
			}
			else {
				indexExtends = -1;
				indexBody--;
				indexImplements--;
			}
			//instance
			List<String> _implments = new ArrayList<String>();
			if(elm.getChild(indexImplements) instanceof SuperinterfacesContext){
				_implments = Getter.listInterfaces( (ParserRuleContext) elm.getChild(indexImplements) );
			} else {
				indexImplements = -1;
				indexBody--;
			}
			String bckPkgName = packageName;
			ASTClass c = new ASTClass(ctx.start, ctx.stop, packageName, name, vis, extendsName, _implments);
			c.setImports(listOfImports);
			packageName = packageName + "." + name;
			listOfClasses.add(c);
			lastClass = c;
			walk((ParserRuleContext) elm.getChild(indexBody));
			if(indexBody > 0) elm.children.remove(indexBody);
			packageName = bckPkgName;
		}
		lastClass = bck;
	}

	@Override
	public void enterFieldDeclaration(@NotNull FieldDeclarationContext ctx) {
		super.enterFieldDeclaration(ctx);
		int indexModifier = 0, indexStatic = 1, indexFinal = 2, indexType = 3, indexName = 4, indexExpr = 5;
		ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
		if(ctx.getChild(indexModifier) instanceof FieldModifierContext){
			vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexModifier));
		} else {
			indexType--;
			indexName--;
			indexExpr--;
			indexModifier = -1;
		}
		if(ctx.getChild(indexStatic) instanceof FieldModifierContext){
			//vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexModifier));
		} else {
			indexType--;
			indexName--;
			indexExpr--;
			indexStatic = -1;
		}
		if(ctx.getChild(indexFinal) instanceof FieldModifierContext){
			//vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexModifier));
		} else {
			indexType--;
			indexName--;
			indexExpr--;
			indexFinal = -1;
		}
		String type = Getter.variableType((ParserRuleContext) ctx.getChild(indexType));
		String name = Getter.variableName((ParserRuleContext) ctx.getChild(indexName));
		ASTRE expr = Getter.attributeExpression((ParserRuleContext) ctx.getChild(indexName));
		ASTAttribute attribute = new ASTAttribute(ctx.start, ctx.stop, vis, type, name, expr);
		lastClass.addAttribute(attribute);
	}

	@Override
	public void enterMethodDeclaration(@NotNull MethodDeclarationContext ctx) {
		int indexAnnotation = 0, indexAccessRight = 0, indexStatic = 1, indexHeader = 2;
		int indexReturn = 0, indexPars = 1, indexThrows = 2;
		ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
		ASTClass.Visibility _static = ASTClass.Visibility.PRIVATE;
		String returnType = "void";
		String methodName = "";
		List<ASTVariable> pars = new ArrayList<ASTVariable>();
		List<String> throwedException = new ArrayList<String>();

		if(ctx.getChild(indexAnnotation) instanceof MethodModifierContext && ctx.getChild(indexAnnotation).getChild(0) instanceof AnnotationContext){
			indexAccessRight++;
			indexStatic++;
			indexHeader++;
		}
		if(ctx.getChild(indexAccessRight) instanceof MethodModifierContext){
			vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexAccessRight));
		} else {
			indexHeader--;
			indexStatic--;
			indexAccessRight = -1;
		}
		if(ctx.getChild(indexStatic) instanceof MethodModifierContext){
			_static = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexStatic));
		} else {
			indexHeader--;
			indexStatic = -1;
		}
		if(ctx.getChild(indexHeader) instanceof MethodHeaderContext) {
			ParserRuleContext header = (ParserRuleContext) ctx.getChild(indexHeader);
			returnType = header.getChild(indexReturn).getText();
			methodName = header.getChild(indexPars).getChild(0).getText();
			pars = Getter.parameterList((ParserRuleContext) header.getChild(indexPars));
			if(header.children.size() > indexThrows && header.getChild(indexThrows) instanceof Throws_Context){
				throwedException = Getter.listThrows((ParserRuleContext) header.getChild(indexThrows));
			} else {
			  	indexThrows = -1;
			}
		} else {
			indexHeader = -1;
		}

		ASTMethod method = new ASTMethod(ctx.start, ctx.stop, methodName, returnType, pars, throwedException, false);
		lastClass.addMethod(method);
		lastMethod = method;
	}

	@Override
	public void enterConstructorDeclaration(@NotNull ConstructorDeclarationContext ctx) {

		int indexAnnotation = 0, indexAccessRight = 0, indexHeader = 1, indexThrows = 2;
		int indexName = 0, indexPars = 2;
		ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
		String returnType = "void";
		String methodName = "";
		List<ASTVariable> pars = new ArrayList<ASTVariable>();
		List<String> throwedException = new ArrayList<String>();
		if(ctx.getChild(indexAnnotation) instanceof ConstructorModifierContext && ctx.getChild(indexAnnotation).getChild(0) instanceof AnnotationContext){
			indexAccessRight++;
			indexHeader++;
			indexThrows++;
		}
		if(ctx.getChild(indexAccessRight) instanceof ConstructorModifierContext){
			vis = Getter.accessRightClass((ParserRuleContext) ctx.getChild(indexAccessRight));
		} else {
			indexHeader--;
			indexThrows--;
			indexAccessRight = -1;
		}
		if(ctx.getChild(indexHeader) instanceof ConstructorDeclaratorContext) {
			ParserRuleContext header = (ParserRuleContext) ctx.getChild(indexHeader);
			methodName = header.getChild(indexName).getText();
			pars = Getter.parameterList((ParserRuleContext) header.getChild(indexPars));
		} else {
			indexHeader = -1;
			indexThrows--;
		}
		if(ctx.getChild(indexThrows) instanceof Throws_Context){
			throwedException = Getter.listThrows((ParserRuleContext) ctx.getChild(indexThrows));
		} else {
			indexThrows = -1;
		}
		ASTConstructor method = new ASTConstructor(ctx.start, ctx.stop, methodName, pars, throwedException);
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
			ASTRE update = getExprState((ParserRuleContext) ctx.getChild(indexUpdate));
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

		if(ctx.getChild(indexType) instanceof VariableModifierContext){
			indexType++;
			indexVar++;
			indexExpr++;
			indexBody++;
		}

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
		//skip the resource, we have a special rule for it
		if(Exists.TryWithResource(ctx)){
			return;
		}

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
	}

	@Override
	public void enterTryWithResourcesStatement(@NotNull TryWithResourcesStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexResources = 1, indexTry = 2, indexCatch = 3, indexFinally = 4;

		ASTTryResources trystm = new ASTTryResources(ctx.start, ctx.stop, Getter.tryResources((ParserRuleContext) ctx.getChild(indexResources)));
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
		if(indexResources > 0) ctx.children.remove(indexResources);
	}

	@Override
	public void enterBlockStatement(@NotNull BlockStatementContext ctx) {
		generateState(ctx);
	}

	public void enterWhileStatement(@NotNull WhileStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexExp = 2, indexBody = 4;

		ASTRE expr = getExprState((ParserRuleContext) ctx.getChild(indexExp));
		ASTWhile whilestm = new ASTWhile(ctx.start, ctx.stop, expr);
		lastMethod.addStms(whilestm);
		lastMethod = whilestm;

		walk((ParserRuleContext) ctx.getChild(indexBody));

		lastMethod = bck;
		if(indexBody > 0) ctx.children.remove(indexBody);
		if(indexExp > 0) ctx.children.remove(indexExp);

	}

	@Override
	public void enterSwitchStatement(@NotNull SwitchStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexExpr = 2, indexCases = 4;

		//expr
		ASTRE expr = getExprState((ParserRuleContext) ctx.getChild(indexExpr));
		ASTSwitch switchstm = new ASTSwitch(ctx.start, ctx.stop, expr);
		lastMethod.addStms(switchstm);

		//cases
		SwitchBlockContext cases = (SwitchBlockContext) ctx.getChild(indexCases);
		int nCases = cases.children.size();
		for(int i = 0; i < nCases; i++){
			if(cases.getChild(i) instanceof TerminalNode){
				continue;
			}
			ASTSwitch.ASTCase casestm = switchstm.new ASTCase(
					((ParserRuleContext)cases.getChild(i)).start,
					((ParserRuleContext)cases.getChild(i)).stop,
					Getter.switchLabel((ParserRuleContext)cases.getChild(i))
			);
			switchstm.addCase(casestm);
			lastMethod = casestm;
			walk((ParserRuleContext)cases.getChild(i));
		}

		lastMethod = bck;
		if(indexCases > 0) ctx.children.remove(indexCases);
		if(indexExpr > 0) ctx.children.remove(indexExpr);
	}


	@Override
	public void enterSynchronizedStatement(@NotNull SynchronizedStatementContext ctx) {
		IASTHasStms bck = lastMethod;
		int indexExpr = 2, indexBody = 4;
		ASTRE expr = getExprState((ParserRuleContext) ctx.getChild(indexExpr));
		ASTSynchronized sync = new ASTSynchronized(ctx.start, ctx.stop, expr);
		lastMethod.addStms(sync);
		lastMethod = sync;
		walk((ParserRuleContext) ctx.getChild(indexBody));
		lastMethod = bck;
		if(indexBody > 0) ctx.children.remove(indexBody);
		if(indexExpr > 0) ctx.children.remove(indexExpr);
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
		else if(Exists.Break(ctx)){
			state = Getter.breakStm(ctx);
		}
		else if(Exists.Throws(ctx)){
			state = Getter.throwsStm(ctx);
		}
		else {
			state = new ASTRE(ctx.start, ctx.stop, REParser.getExpr(ctx));
		}
		return state;
	}

	protected ASTRE getExprState(ParserRuleContext ctx){
		return new ASTRE(ctx.start, ctx.stop, REParser.getExpr(ctx));
	}

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
