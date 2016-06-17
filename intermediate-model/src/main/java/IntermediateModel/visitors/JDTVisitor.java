package IntermediateModel.visitors;

import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.*;
import IntermediateModel.structure.expression.NotYetImplemented;
import IntermediateModel.visitors.utility.Getter;
import IntermediateModel.visitors.utility.REParserJDT;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class JDTVisitor extends ASTVisitor {

	public List<ASTClass> listOfClasses = new ArrayList<ASTClass>();
	private CompilationUnit cu;
	private String packageName = "";
	private List<ASTImport> listOfImports = new ArrayList<>();
	private ASTClass lastClass;
	private IASTHasStms lastMethod;

	private Stack<ASTClass> stackClasses = new Stack<>();
	private Stack<String> stackPackage = new Stack<>();
	private Stack<IASTHasStms> stackSwitch = new Stack<>();
	private Stack<ASTSwitch> casewitch = new Stack<>();


	public JDTVisitor(CompilationUnit cu) {
		this.cu = cu;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		packageName = node.getName().getFullyQualifiedName();
		stackPackage.add(packageName);
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		listOfImports.add( new ASTImport(start, stop, false, node.getName().getFullyQualifiedName()) );
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//class generation
		String className = node.getName().getFullyQualifiedName();
		ASTClass.Visibility visibility = ASTClass.Visibility.PRIVATE;
		if(node.modifiers().size() > 0){
			int i = 0;
			while(!(node.modifiers().get(i) instanceof Modifier)){
				i++;
			}
			Modifier m = (Modifier) node.modifiers().get(i);
			visibility = Getter.visibility(m.toString());
		}
		String superClass = node.getSuperclassType() == null ? "Object" : node.getSuperclassType().toString();
		List<String> superInterfaces = new ArrayList<>();
		for(Object ost : node.superInterfaceTypes()){
			if(ost instanceof SimpleType) {
				SimpleType st = (SimpleType) ost;
				superInterfaces.add(st.getName().getFullyQualifiedName());
			}
			else {
				ParameterizedType pt = (ParameterizedType) ost;
				superInterfaces.add(pt.getType().toString());
			}
		}
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTClass c = new ASTClass(start, stop, packageName, className, visibility, superClass, superInterfaces);
		c.setImports(listOfImports);
		//attributes of the class
		for(FieldDeclaration f : node.getFields()){
			ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
			if(f.modifiers().size() > 0){
				int i = 0;
				while(i < f.modifiers().size() && !(f.modifiers().get(i) instanceof Modifier)){
					i++;
				}
				Modifier m = (Modifier) f.modifiers().get(i);
				vis = Getter.visibility(m.toString());
			}
			String type = f.getType().toString();
			String name = "";
			ASTRE expr = null;
			for(Object ovf : f.fragments()){
				VariableDeclarationFragment vf = (VariableDeclarationFragment) ovf;
				name = vf.getName().getFullyQualifiedName();
				expr = getExprState(vf.getInitializer());
			}
			int ss = 0; int st = 0;
			ss = f.getStartPosition();
			st = ss + f.getLength();
			ASTAttribute attribute = new ASTAttribute(ss, st, vis, type, name, expr);
			c.addAttribute(attribute);
		}
		packageName = packageName + "." + className;
		stackPackage.push(packageName);

		listOfClasses.add(c);
		stackClasses.push(c);
		lastClass = c;
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		if(stackClasses.size() > 0) {
			ASTClass c = stackClasses.pop();
			if (c.equals(lastClass) && stackClasses.size() > 0) {
				lastClass = stackClasses.peek();
			}
		}
		if(stackPackage.size() > 0) {
			String t = stackPackage.pop();
			if (t.equals(packageName) && stackPackage.size() > 0) {
				packageName = stackPackage.peek();
			}

		}
	}

	@Override
	public boolean visit(Initializer node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTStatic s = new ASTStatic(start, stop);
		lastMethod = s;
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		String methodName = node.getName().getFullyQualifiedName();
		String returnType = node.getReturnType2() == null ? null : node.getReturnType2().toString();
		List<ASTVariable> pars = new ArrayList<ASTVariable>();
		List<String> throwedException = new ArrayList<String>();
		//throws
		for(Object oth : node.thrownExceptionTypes()){
			SimpleType th = (SimpleType) oth;
			throwedException.add(th.getName().getFullyQualifiedName());
		}
		//pars
		for(Object op : node.parameters()){
			SingleVariableDeclaration p = (SingleVariableDeclaration) op;
			int ss, st;
			ss = p.getStartPosition();
			st = ss + p.getLength();
			ASTVariable par = new ASTVariable(ss, st, p.getName().getFullyQualifiedName(), p.getType().toString());
			pars.add(par);
		}
		//is syncronized
		boolean isSync = false;
		for(Object m : node.modifiers()){
			if(m instanceof Modifier){
				Modifier modifier = (Modifier)m;
				if(modifier.isSynchronized()){
					isSync = true;
				}
			}
		}

		IASTMethod method = null;
		if(returnType == null){
			//constructor
			method = new ASTConstructor(start, stop, methodName, pars, throwedException);
		} else {
			method = new ASTMethod(start, stop, methodName, returnType, pars, throwedException, isSync);
		}

		lastClass.addMethod(method);
		lastMethod = method;
		return true;
	}


	@Override
	public boolean visit(ForStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTFor forstm = new ASTFor(start, stop);
		lastMethod.addStms(forstm);
		lastMethod = forstm;
		//init expr
		if(node.initializers().size() > 0){
			List<ASTRE> init = new ArrayList<>();
			for(Object oInit : node.initializers()){
				ASTNode expr = (ASTNode) oInit;
				ASTRE initExpr = getExprState(expr);
				init.add(initExpr);
			}
			forstm.setInit(init);
		}
		//guard expr
		if(node.getExpression() != null){
			ASTRE guard = getExprState(node.getExpression());
			forstm.setExpr(guard);
		}
		//update expr
		if(node.updaters().size() > 0){
			List<ASTRE> update = new ArrayList<>();
			for(Object oInit : node.updaters()){
				ASTNode expr = (ASTNode) oInit;
				ASTRE updateExpr = getExprState(expr);
				update.add(updateExpr);
			}
			forstm.setPost(update);
		}
		node.getBody().accept(this);
		node.setBody(createEmpty());
		lastMethod = bck;
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		SingleVariableDeclaration v = node.getParameter();
		int vstart = v.getStartPosition();
		int vstop = vstart + v.getLength();
		String vname = v.getName().getIdentifier();
		String vtype = v.getType().toString();
		ASTVariable var = new ASTVariable(vstart, vstop, vname, vtype);
		ASTRE expr = getExprState(node.getExpression());

		ASTForEach foreach = new ASTForEach(start,stop, var, expr);
		lastMethod.addStms(foreach);
		lastMethod = foreach;

		node.getBody().accept(this);
		node.setBody(createEmpty());

		lastMethod = bck;
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTRE guard = getExprState(node.getExpression());
		ASTIf ifstm = new ASTIf(start, stop, guard);
		lastMethod.addStms(ifstm);

		int startThen = node.getThenStatement().getStartPosition();
		int stopThen = startThen + node.getThenStatement().getLength();
		ASTIf.ASTIfStms then = ifstm.new ASTIfStms( startThen, stopThen);
		ifstm.setIfBranch(then);
		lastMethod = then;

		node.getThenStatement().accept(this);

		if(node.getElseStatement() != null){
			int startElse = node.getElseStatement().getStartPosition();
			int stopElse = startElse + node.getElseStatement().getLength();
			ASTIf.ASTElseStms elseBranch = ifstm.new ASTElseStms( startElse, stopElse);
			ifstm.setElseBranch(elseBranch);
			lastMethod = elseBranch;
			node.getElseStatement().accept(this);
		}

		node.setThenStatement(createEmpty());
		node.setElseStatement(createEmpty());

		lastMethod = bck;
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTTry elm;
		if(node.resources().size() > 0){
			//try with resources
			List<ASTRE> resources = new ArrayList<>();
			for(Object r : node.resources()){
				VariableDeclarationExpression expr = (VariableDeclarationExpression)  r;
				resources.add( getExprState(expr) );
			}
			elm = new ASTTryResources(start, stop, resources);
		} else {
			elm = new ASTTry(start,stop);
		}
		lastMethod.addStms(elm);

		//try branch
		int tStart, tStop;
		tStart = node.getBody().getStartPosition();
		tStop = tStart + node.getBody().getLength();
		ASTTry.ASTTryBranch tryBranch = elm.new ASTTryBranch(tStart, tStop);
		elm.setTryBranch(tryBranch);
		lastMethod = tryBranch;
		node.getBody().accept(this);

		//catch
		for(Object oC : node.catchClauses()){
			CatchClause c = (CatchClause) oC;
			int cStart, cStop;
			cStart = c.getStartPosition();
			cStop = cStart + c.getLength();

			SingleVariableDeclaration exception = c.getException();
			int eStart, eStop;
			eStart = exception.getStartPosition();
			eStop = eStart + exception.getLength();

			ASTVariable ex = new ASTVariable(eStart, eStop, exception.getName().getFullyQualifiedName(), exception.getType().toString() );
			ASTTry.ASTCatchBranch catchBranch = elm.new ASTCatchBranch(cStart, cStop, ex);
			elm.addCatchBranch(catchBranch);
			lastMethod = catchBranch;
			c.getBody().accept(this);
			c.setBody(createEmptyBlock());
		}

		//finally
		if(node.getFinally() != null){
			Block endly = node.getFinally();
			int endStart, endStop;
			endStart = endly.getStartPosition();
			endStop = endStart + endly.getLength();
			ASTTry.ASTFinallyBranch endBranch = elm.new ASTFinallyBranch(endStart, endStop);
			elm.setFinallyBranch(endBranch);
			lastMethod = endBranch;
			endly.accept(this);
		}

		node.setBody(createEmptyBlock());
		node.setFinally(createEmptyBlock());

		lastMethod = bck;
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTRE expr = getExprState(node.getExpression());
		ASTWhile whilestm = new ASTWhile(start, stop, expr);
		lastMethod.addStms(whilestm);
		lastMethod = whilestm;

		node.getBody().accept(this);

		node.setBody(createEmpty());
		lastMethod = bck;
		return true;
	}

	//do while

	@Override
	public boolean visit(DoStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTRE expr = getExprState(node.getExpression());
		ASTDoWhile whilestm = new ASTDoWhile(start, stop, expr);
		lastMethod.addStms(whilestm);
		lastMethod = whilestm;

		node.getBody().accept(this);

		node.setBody(createEmpty());
		lastMethod = bck;
		return true;
	}

	//swhitch

	@Override
	public boolean visit(SwitchStatement node) {
		IASTHasStms bck = lastMethod;
		stackSwitch.push(bck);
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTRE expr = getExprState(node.getExpression());
		ASTSwitch switchstm = new ASTSwitch(start, stop, expr);
		lastMethod.addStms(switchstm);
		casewitch.push(switchstm);

		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		//i am sure it exists otherwise is not a compilable file
		ASTSwitch top = casewitch.peek();
		List<String> listLabels = new ArrayList<>();
		listLabels.add(
				node.getExpression() == null ? "default" : node.getExpression().toString()
		);
		ASTSwitch.ASTCase casestm = top.new ASTCase( start, stop, listLabels );
		top.addCase(casestm);
		lastMethod = casestm;
		return true;
	}

	@Override
	public void endVisit(SwitchStatement node) {
		lastMethod = stackSwitch.pop();
		casewitch.pop();
	}

	//sync


	@Override
	public boolean visit(SynchronizedStatement node) {
		IASTHasStms bck = lastMethod;
		int start = node.getStartPosition();
		int stop = start + node.getLength();

		ASTRE expr = getExprState(node.getExpression());
		ASTSynchronized sync = new ASTSynchronized(start, stop, expr);
		lastMethod.addStms(sync);
		lastMethod = sync;
		node.getBody().accept(this);
		node.setBody(createEmptyBlock());

		lastMethod = bck;
		return true;
	}


	//special RE
	@Override
	public boolean visit(BreakStatement node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		lastMethod.addStms(new ASTBreak(start,stop));
		return true;
	}
	@Override
	public boolean visit(ContinueStatement node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		lastMethod.addStms(new ASTContinue(start,stop));
		return true;
	}
	@Override
	public boolean visit(ReturnStatement node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		lastMethod.addStms(new ASTReturn(start,stop, getExprState(node.getExpression())));
		return true;
	}

	//RE expr
	@Override
	public boolean visit(ExpressionStatement node) {
		ASTRE re = getExprState(node);
		lastMethod.addStms(re);
		return true;
	}

	protected ASTRE getExprState(ASTNode ctx){
		if(ctx == null)
			return null;
		int start = ctx.getStartPosition();
		int stop = start + ctx.getLength();
		return new ASTRE(start, stop, REParserJDT.getExpr(ctx));
	}

	//Helper to nullify objects
	private Statement createEmpty(){
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		return (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
	}
	private Block createEmptyBlock(){
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		return (Block) rewriter.createStringPlaceholder("", ASTNode.BLOCK);
	}

	private void createEmptyExpr(ASTNode node){
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		rewriter.replace(node, null, null);
	}


}
