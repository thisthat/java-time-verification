package IntermediateModel.visitors;

import IntermediateModel.interfaces.ASTSrc;
import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.*;
import IntermediateModel.visitors.utility.Getter;
import org.antlr.v4.runtime.misc.ObjectEqualityComparator;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.compiler.ast.*;

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


	public JDTVisitor(CompilationUnit cu) {
		this.cu = cu;
		ASTSrc.getInstance().setSource(cu.toString().toCharArray());
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
			Modifier m = (Modifier) node.modifiers().get(0);
			visibility = Getter.visibility(m.toString());
		}
		String superClass = node.getSuperclassType() == null ? "Object" : node.getSuperclassType().toString();
		List<String> superInterfaces = new ArrayList<>();
		for(Object ost : node.superInterfaceTypes()){
			SimpleType st = (SimpleType) ost;
			superInterfaces.add(st.getName().getFullyQualifiedName());
		}
		int start = node.getStartPosition();
		int stop = start + node.getLength();
		ASTClass c = new ASTClass(start, stop, packageName, className, visibility, superClass, superInterfaces);
		c.setImports(listOfImports);
		//attributes of the class
		for(FieldDeclaration f : node.getFields()){
			ASTClass.Visibility vis = ASTClass.Visibility.PRIVATE;
			if(f.modifiers().size() > 0){
				Modifier m = (Modifier) f.modifiers().get(0);
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

		IASTMethod method = null;
		if(returnType == null){
			//constructor
			method = new ASTConstructor(start, stop, methodName, pars, throwedException);
		} else {
			method = new ASTMethod(start, stop, methodName, returnType, pars, throwedException);
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
			int stopElse = startThen + node.getElseStatement().getLength();
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
			c.setBody((Block) createEmpty());
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

		node.setBody((Block) createEmpty());
		node.setFinally((Block) createEmpty());

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

		node.setBody(createEmpty());
		lastMethod = bck;
		return true;
	}

	//do while
	//swhitch
	//sync

	@Override
	public boolean visit(ExpressionStatement node) {
		ASTRE re = getExprState(node);
		lastMethod.addStms(re);
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		//new Obj
		//System.out.println( node.toString() );
		return super.visit(node);
	}

	protected ASTRE getExprState(ASTNode ctx){
		if(ctx == null)
			return null;
		int start = ctx.getStartPosition();
		int stop = start + ctx.getLength();
		return new ASTRE(start, stop, null);
	}

	private Statement createEmpty(){
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		return (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
	}
}
