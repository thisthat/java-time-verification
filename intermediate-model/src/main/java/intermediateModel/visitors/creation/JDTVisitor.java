package intermediateModel.visitors.creation;

import intermediateModel.interfaces.IASTHasStms;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.*;
import intermediateModel.visitors.creation.utility.Getter;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
	private String path;

	private static Map<String, List<ASTClass>> cache = new HashMap<>();

	public static List<ASTClass> parse(String filename){
		if(cache.containsKey(filename)){
			return cache.get(filename);
		}
		Java2AST a = null;
		try {
			a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
		} catch (IOException e) {
		} catch (ParseErrorsException e) {
		}
		CompilationUnit result = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(result, filename);
		result.accept(v);
		cache.put(filename, v.listOfClasses);
		return v.listOfClasses;
	}

	public JDTVisitor(CompilationUnit cu, String path) {
		this.cu = cu;
		this.path = path;
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
		String suffix = "";
		if(node.toString().trim().endsWith("*;")){
			suffix = ".*";
		}
		listOfImports.add( new ASTImport(start, stop, false, node.getName().getFullyQualifiedName() + suffix ));
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//class generation
		String className = node.getName().getFullyQualifiedName();
		ASTClass.Visibility visibility = ASTClass.Visibility.PRIVATE;
		if(node.modifiers().size() > 0){
			int i = 0;
			while(i < node.modifiers().size() && !(node.modifiers().get(i) instanceof Modifier)){
				i++;
			}
			if(i != node.modifiers().size()) {
				Modifier m = (Modifier) node.modifiers().get(i);
				visibility = Getter.visibility(m.toString());
			}
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
		c.setPath(path);
		c.setImports(listOfImports);
		//attributes of the class
		for(FieldDeclaration f : node.getFields()){
			ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
			if(f.modifiers().size() > 0){
				int i = 0;
				while(i < f.modifiers().size() && !(f.modifiers().get(i) instanceof Modifier)){
					i++;
				}
				if(i < f.modifiers().size()) {
					Modifier m = (Modifier) f.modifiers().get(i);
					vis = Getter.visibility(m.toString());
				}
				boolean isAbs = false;
				for(i = 0; i < f.modifiers().size(); i++){
					if(f.modifiers().get(i) instanceof Modifier){
						Modifier m = (Modifier) f.modifiers().get(i);
						if(m.isAbstract()) isAbs = true;
					}
				}
				c.setAbstract(isAbs);
			}
			String type = f.getType().toString();
			String name = "";
			ASTRE expr = null;
			for(Object ovf : f.fragments()){
				VariableDeclarationFragment vf = (VariableDeclarationFragment) ovf;
				name = vf.getName().getFullyQualifiedName();
				expr = getExprState(vf.getInitializer());
				if(vf.getInitializer() instanceof LambdaExpression ) vf.getInitializer().delete(); //avoid lambda interfeer with remaning
			}
			int ss = 0; int st = 0;
			ss = f.getStartPosition();
			st = ss + f.getLength();
			ASTAttribute attribute = new ASTAttribute(ss, st, vis, type, name, expr);
			c.addAttribute(attribute);
		}
		c.setInterface(node.isInterface());
		packageName = packageName + "." + className;
		stackPackage.push(packageName);

		c.setParent(stackClasses.size() > 0 ? stackClasses.peek() : null);

		listOfClasses.add(c);
		stackClasses.push(c);
		lastClass = c;
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		String className = node.getName().getFullyQualifiedName();
		ASTClass.Visibility visibility = ASTClass.Visibility.PRIVATE;
		if(node.modifiers().size() > 0){
			int i = 0;
			while(i < node.modifiers().size() && !(node.modifiers().get(i) instanceof Modifier)){
				i++;
			}
			if(i != node.modifiers().size()) {
				Modifier m = (Modifier) node.modifiers().get(i);
				visibility = Getter.visibility(m.toString());
			}
		}
		String superClass = "Object";
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
		c.setPath(path);
		c.setImports(listOfImports);
		//attributes of the class
		for(Object fo : node.bodyDeclarations()){
			if(!(fo instanceof FieldDeclaration)){
				continue;
			}
			FieldDeclaration f = (FieldDeclaration) fo;
			ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
			if(f.modifiers().size() > 0){
				int i = 0;
				while(i < f.modifiers().size() && !(f.modifiers().get(i) instanceof Modifier)){
					i++;
				}
				if(i < f.modifiers().size()) {
					Modifier m = (Modifier) f.modifiers().get(i);
					vis = Getter.visibility(m.toString());
				}
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
		//const of enum
		for(Object co : node.enumConstants()){
			if(!(co instanceof  EnumConstantDeclaration)) continue;
			EnumConstantDeclaration cons = (EnumConstantDeclaration) co;
			String type = c.getName();
			String name = cons.getName().getFullyQualifiedName();
			int ss = 0; int st = 0;
			ss = cons.getStartPosition();
			st = ss + cons.getLength();
			ASTRE expr = null;

			ASTAttribute attribute = new ASTAttribute(ss, st, ASTClass.Visibility.PUBLIC, type, name, expr);
			c.addAttribute(attribute);
		}
		packageName = packageName + "." + className;
		stackPackage.push(packageName);

		c.setParent(stackClasses.size() > 0 ? stackClasses.peek() : null);

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
	public void endVisit(EnumDeclaration node) {
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
	public void endVisit(AnonymousClassDeclaration node) {
		if(stackClasses.size() > 0) {
			ASTClass c = stackClasses.pop();
			if (c.equals(lastClass) && stackClasses.size() > 0) {
				lastClass = stackClasses.peek();
			}
		}
		//test

	}

	@Override
	public boolean visit(Initializer node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTStatic s = new ASTStatic(start, stop);
		lastClass.addStaticInit(s);
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
		boolean isAbs = false;
		boolean isStatic = false;
		for(Object m : node.modifiers()){
			if(m instanceof Modifier){
				Modifier modifier = (Modifier)m;
				if(modifier.isSynchronized()){
					isSync = true;
				}
				if(modifier.isAbstract()){
					isAbs = true;
				}
				if(modifier.isStatic()){
					isStatic = true;
				}
			}
		}

		IASTMethod method = null;
		if(returnType == null){
			//constructor
			method = new ASTConstructor(start, stop, methodName, pars, throwedException);
		} else {
			method = new ASTMethod(start, stop, methodName, returnType, pars, throwedException, isSync, isAbs, isStatic);
		}
		lastClass.addMethod(method);
		lastMethod = method;
		//stackMethods.push(method);
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
	public boolean visit(VariableDeclarationStatement node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		String type = node.getType().toString();
		IASTHasStms bck = lastMethod;
		for(Object o : node.fragments()){
			if(o instanceof VariableDeclarationFragment){
				VariableDeclarationFragment v = (VariableDeclarationFragment)o;
				ASTRE re = new ASTRE(start, stop,
						new ASTVariableDeclaration(start, stop, type,
								getExpr(v.getName()), getExpr(v.getInitializer()))
						);
				/*handle special hidden classes
				final boolean[] found = {false};
				final ASTHiddenClass[] obj = {null};
				re.visit(new DefaultASTVisitor(){
					@Override
					public void enterASTNewObject(ASTNewObject elm) {
						found[0] = elm.getHiddenClass() != null;
						obj[0] = elm.getHiddenClass();
					}
				});
				if(found[0]){
					stackClasses.push(obj[0]);
					lastClass = obj[0];
				}*/
				bck.addStms(re);
			}
		}
		lastMethod = bck;
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		IASTHasStms bck = lastMethod;
		ASTRE re = getExprState(node);
		bck.addStms(re);
		lastMethod = bck;
		return true;
	}


	@Override
	public boolean visit(ThrowStatement node) {
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTThrow _throw = new ASTThrow(start, stop, getExprState(node.getExpression()) );
		lastMethod.addStms(_throw);
		return true;
	}

	protected ASTRE getExprState(ASTNode ctx){
		if(ctx == null)
			return null;
		int start = ctx.getStartPosition();
		int stop = start + ctx.getLength();
		ASTRE expr =  new ASTRE(start, stop, getExpr(ctx));
		return expr;
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

	public IASTRE getExpr(ASTNode node){
		if(node == null){
			return null;
		}
		if(node instanceof ExpressionStatement){
			return handleExpressionStatement((ExpressionStatement)node);
		} else if(node instanceof VariableDeclarationExpression){
			return variableDeclaration((VariableDeclarationExpression) node);
		} else {
			return handleExpression((Expression) node);
		}

	}

	private IASTRE handleExpressionStatement(ExpressionStatement exprStm){
		return handleExpression(exprStm.getExpression());
	}

	private IASTRE handleExpression(Expression expr){
		if(expr instanceof SimpleName){
			return simpleName((SimpleName) expr);
		} else if(expr instanceof MethodInvocation){
			return methodInvocation((MethodInvocation)expr);
		} else if(expr instanceof PostfixExpression){
			return postFix((PostfixExpression)expr);
		} else if(expr instanceof PrefixExpression){
			return preFix((PrefixExpression)expr);
		} else if(expr instanceof Assignment){
			return assignment((Assignment)expr);
		} else if(expr instanceof StringLiteral){
			return literal((StringLiteral) expr );
		} else if(expr instanceof BooleanLiteral){
			return literal((BooleanLiteral) expr );
		} else if(expr instanceof NumberLiteral){
			return literal((NumberLiteral) expr );
		} else if(expr instanceof ThisExpression){
			return literal((ThisExpression) expr );
		} else if(expr instanceof NullLiteral){
			return literal((NullLiteral) expr );
		} else if(expr instanceof TypeLiteral){
			return literal((TypeLiteral) expr );
		} else if(expr instanceof CharacterLiteral){
			return literal((CharacterLiteral) expr );
		} else if(expr instanceof ClassInstanceCreation){
			return newObject((ClassInstanceCreation)expr);
		} else if(expr instanceof InfixExpression){
			return mathExpression((InfixExpression) expr);
		} else if(expr instanceof FieldAccess){
			return fieldAccess((FieldAccess) expr);
		} else if(expr instanceof QualifiedName){
			return fieldAccess((QualifiedName) expr);
		} else if(expr instanceof VariableDeclarationExpression) {
			return variableDeclaration((VariableDeclarationExpression)expr);
		} else if(expr instanceof ParenthesizedExpression){
			return getExpr(((ParenthesizedExpression) expr).getExpression());
		} else if(expr instanceof CastExpression){
			return cast((CastExpression) expr);
		} else if(expr instanceof ConditionalExpression){
			return conditional((ConditionalExpression) expr);
		} else if(expr instanceof ArrayCreation){
			return arrayCreation((ArrayCreation) expr);
		} else if(expr instanceof ArrayAccess){
			return literal((ArrayAccess) expr);
		} else if(expr instanceof ArrayInitializer){
			return arrayInitializer((ArrayInitializer) expr);
		} else if(expr instanceof SuperMethodInvocation){
			return superInvocation( (SuperMethodInvocation) expr );
		} else if(expr instanceof InstanceofExpression){
			return instanceOfExpression( (InstanceofExpression) expr );
		}

		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new NotYetImplemented(start,stop);
	}

	private IASTRE superInvocation(SuperMethodInvocation expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String name = expr.getName().getFullyQualifiedName();
		IASTRE exprCallee = new ASTLiteral(start, start+5, "super");
		List<IASTRE> pars = new ArrayList<>();
		for(Object p : expr.arguments()){
			pars.add(
					getExpr( (ASTNode) p )
			);
		}
		return new ASTMethodCall(start,stop, name, exprCallee, pars );
	}

	private IASTRE arrayInitializer(ArrayInitializer expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		List<IASTRE> elms = new ArrayList<>();
		for(Object o : expr.expressions()){
			if(o instanceof ASTNode){
				elms.add( getExpr((ASTNode)o));
			}
		}
		return new ASTArrayInitializer(start,stop, elms);
	}

	private IASTRE arrayCreation(ArrayCreation expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();

		String type = expr.getType().getElementType().toString();
		List<IASTRE> pars = new ArrayList<>();

		for(Object o : expr.dimensions()){
			if(o instanceof Expression){
				pars.add( getExpr((Expression)o) );
			}
		}

		return new ASTNewObject(start,stop, type, true, pars);
	}

	private IASTRE conditional(ConditionalExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();

		IASTRE e = getExpr(expr.getExpression());
		IASTRE ethen = getExpr(expr.getThenExpression());
		IASTRE eelse = getExpr(expr.getElseExpression());

		return new ASTConditional(start,stop, e, ethen, eelse);
	}

	private IASTRE cast(CastExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		IASTRE e = getExpr(expr.getExpression());
		return new ASTCast(start,stop, type, e);
	}

	private IASTRE variableDeclaration(VariableDeclarationExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		IASTRE ret = null;
		if(expr.fragments().size() > 1){
			//multiple definition
			List<IASTRE> vars = new ArrayList<>();
			for(Object o :expr.fragments()){
				VariableDeclarationFragment subVar = (VariableDeclarationFragment) o;
				int vStart, vStop;
				vStart = subVar.getStartPosition();
				vStop = vStart + subVar.getLength();
				IASTRE name = getExpr(subVar.getName());
				IASTRE e = getExpr(subVar.getInitializer());
				ASTVariableDeclaration v = new ASTVariableDeclaration(vStart, vStop, type, name, e);
				vars.add(v);
			}
			ASTVariableMultipleDeclaration multiplevars = new ASTVariableMultipleDeclaration(start,stop,type, vars);
			ret = multiplevars;
		} else {
			for(Object o :expr.fragments()){
				VariableDeclarationFragment subVar = (VariableDeclarationFragment) o;
				int vStart, vStop;
				vStart = subVar.getStartPosition();
				vStop = vStart + subVar.getLength();
				IASTRE name = getExpr(subVar.getName());
				IASTRE e = getExpr(subVar.getInitializer());
				ASTVariableDeclaration v = new ASTVariableDeclaration(vStart, vStop, type, name, e);
				ret = v;
			}

		}
		return ret;
	}

	private IASTRE fieldAccess(FieldAccess expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE where;
		String who;
		where = getExpr(expr.getExpression());
		who = expr.getName().getIdentifier();
		return new ASTAttributeAccess(start,stop, where, who);
	}

	private IASTRE fieldAccess(QualifiedName expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE where;
		String who;
		where = getExpr(expr.getQualifier());
		who = expr.getName().getIdentifier();
		return new ASTAttributeAccess(start,stop, where, who);
	}

	private IASTRE instanceOfExpression(InstanceofExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.OPERATOR op = IASTRE.OPERATOR.instanceOf;
		IASTRE l = getExpr(expr.getLeftOperand());
		Type t = expr.getRightOperand();
		IASTRE r = new ASTLiteral(t.getStartPosition(), t.getStartPosition()+t.getLength(), t.toString());
		return new ASTBinary(start,stop, l, r, op);
	}

	private IASTRE mathExpression(InfixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.OPERATOR op = getOperator(expr.getOperator().toString());
		IASTRE l = getExpr(expr.getLeftOperand());
		IASTRE r = getExpr(expr.getRightOperand());
		ASTBinary bin = new ASTBinary(start,stop, l, r, op);
		IASTRE prev = bin;
		for(Object o : expr.extendedOperands()){
			ASTNode extExpr = (ASTNode) o;
			IASTRE extended = getExpr(extExpr);
			int extstart, extstop;
			extstart = extExpr.getStartPosition();
			extstop = extstart + extExpr.getLength();
			ASTBinary ext = new ASTBinary(extstart, extstop, prev, extended, op);
			prev = ext;
		}
		return prev;
	}

	private IASTRE newObject(ClassInstanceCreation expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		String type = expr.getType().toString();
		List<IASTRE> pars = new ArrayList<>();
		for(Object p : expr.arguments()){
			pars.add(
					getExpr((ASTNode) p)
			);
		}
		ASTNewObject obj = new ASTNewObject(start,stop, type, false, pars);
		if(expr.getAnonymousClassDeclaration() != null){
			AnonymousClassDeclaration hc = expr.getAnonymousClassDeclaration();
			int st, sp;
			st = hc.getStartPosition();
			sp = st + hc.getLength();
			ASTHiddenClass c = new ASTHiddenClass(st, sp);
			obj.setHiddenClass(c);
			//attributes of the hidden class + methods
			for(Object node : hc.bodyDeclarations()){
				if(node instanceof FieldDeclaration){
					FieldDeclaration f = (FieldDeclaration) node;
					ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
					if(f.modifiers().size() > 0){
						int i = 0;
						while(i < f.modifiers().size() && !(f.modifiers().get(i) instanceof Modifier)){
							i++;
						}
						Modifier m = (Modifier) f.modifiers().get(i);
						vis = Getter.visibility(m.toString());
					}
					String typeF = f.getType().toString();
					String name = "";
					ASTRE exprF = null;
					for(Object ovf : f.fragments()){
						VariableDeclarationFragment vf = (VariableDeclarationFragment) ovf;
						name = vf.getName().getFullyQualifiedName();
						exprF = getExprState(vf.getInitializer());
					}
					int ssF = 0; int stF = 0;
					ssF = f.getStartPosition();
					stF = ssF + f.getLength();
					ASTAttribute attribute = new ASTAttribute(ssF, stF, vis, typeF , name, exprF);
					c.addAttribute(attribute);
				}
				if(node instanceof MethodDeclaration){
					ASTClass bck = lastClass;
					lastClass = c;
					((MethodDeclaration) node).accept(this);
					lastClass = bck;
				}
			}
			expr.setAnonymousClassDeclaration(null);
		}
		return obj;
	}

	private IASTRE literal(NullLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}


	private IASTRE literal(CharacterLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private IASTRE literal(ArrayAccess expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.getArray().toString());
	}

	private IASTRE literal(TypeLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private IASTRE literal(BooleanLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private IASTRE literal(NumberLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private IASTRE literal(ThisExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.toString());
	}

	private IASTRE literal(StringLiteral expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.getEscapedValue());
	}

	private IASTRE assignment(Assignment expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.OPERATOR op = getOperator(expr.getOperator().toString());
		IASTRE l = getExpr(expr.getLeftHandSide());
		IASTRE r = getExpr(expr.getRightHandSide());
		return new ASTAssignment(start, stop, l, r, op );
	}

	private IASTRE postFix(PostfixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE.ADDDEC op = IASTRE.ADDDEC.increment;
		if(expr.getOperator().equals("--")){
			op = IASTRE.ADDDEC.decrement;
		}
		IASTRE e = getExpr(expr.getOperand());
		return new ASTPostOp(start, stop, e, op);
	}
	private IASTRE preFix(PrefixExpression expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		IASTRE e = getExpr(expr.getOperand());
		//unary expr not
		if(expr.getOperator().equals("!")){
			return new ASTUnary(start,stop, IASTRE.OPERATOR.not, e);
		}
		//unary -num
		if(expr.getOperator().equals("-")){
			return new ASTUnary(start,stop, IASTRE.OPERATOR.minus, e);
		}
		//normal pre inc/dec
		IASTRE.ADDDEC op = IASTRE.ADDDEC.increment;
		if(expr.getOperator().equals("--")){
			op = IASTRE.ADDDEC.decrement;
		}
		return new ASTPreOp(start, stop, e, op);
	}

	private IASTRE simpleName(SimpleName expr) {
		int start, stop;
		start = expr.getStartPosition();
		stop = start + expr.getLength();
		return new ASTLiteral(start, stop, expr.getIdentifier());
	}

	private IASTRE methodInvocation(MethodInvocation node) {
		int start, stop;
		start = node.getStartPosition();
		stop = start + node.getLength();
		String name = node.getName().getFullyQualifiedName();
		IASTRE exprCallee = getExpr(node.getExpression());
		List<IASTRE> pars = new ArrayList<>();
		for(Object p : node.arguments()){
			pars.add(
					getExpr( (ASTNode) p )
			);
		}
		return new ASTMethodCall(start,stop, name, exprCallee, pars );
	}

	public IASTRE.OPERATOR getOperator(String op){
		switch (op){
			case "=": return IASTRE.OPERATOR.equal;
			case "+": return IASTRE.OPERATOR.plus;
			case "-": return IASTRE.OPERATOR.minus;
			case "/": return IASTRE.OPERATOR.div;
			case "*": return IASTRE.OPERATOR.mul;
			case ">": return IASTRE.OPERATOR.greater;
			case ">=": return IASTRE.OPERATOR.greaterEqual;
			case "<": return IASTRE.OPERATOR.less;
			case "<=": return IASTRE.OPERATOR.lessEqual;
			case "==": return IASTRE.OPERATOR.equality;
			case "||": return IASTRE.OPERATOR.or;
			case "&&": return IASTRE.OPERATOR.and;
			case "%": return IASTRE.OPERATOR.mod;
		}
		return IASTRE.OPERATOR.equal;
	}

}