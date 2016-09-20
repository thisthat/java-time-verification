package IntermediateModelHelper.indexing;

import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexEnv;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import IntermediateModelHelper.indexing.structure.IndexSyncCall;
import IntermediateModelHelper.types.ResolveTypes;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTAssignment;
import intermediateModel.structure.expression.ASTAttributeAccess;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class created the {@link IndexSyncCall} for a given {@link ASTClass}.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingSyncBlock extends ParseIM {

	MongoConnector mongo;
	ASTClass _c = null;
	String lastMethodName = "";
	List<String> signatureLastMethodName = new ArrayList<>();
	List<IndexSyncBlock> output = new ArrayList<>();
	List<IndexData> imports = new ArrayList<>();

	public IndexingSyncBlock() {
		String dbname = MongoOptions.getInstance().getDbName();
		mongo = MongoConnector.getInstance(dbname);
	}

	public IndexingSyncBlock(MongoConnector mongo) {
		this.mongo = mongo;
	}

	/**
	 * Start the indexing the calls of synchronized method of a {@link ASTClass}.
	 * It force to delete the index structure from the DB and recreate it.
	 *
	 * @param c	Class to analyse.
	 * @return	The list of indexes of sync calls of the class.
	 */
	public List<IndexSyncBlock> index(ASTClass c){
		return index(c, false);
	}
	/**
	 * Start the indexing the calls of synchronized method of a {@link ASTClass}.
	 * It force to delete the index structure from the DB and to recreate it if the <i>forceReindex</i> flag is set to true.
	 * @param c	Class to analyze
     * @param forceReindex flag to force the recreation of the index
	 * @return	The list of indexes of sync calls of the class.
	 */
	public List<IndexSyncBlock> index(ASTClass c, boolean forceReindex) {
		this._c = c;
		if(mongo.existSyncBlockIndex(c)){
			if(forceReindex){
				mongo.deleteSyncBlock(c);
			} else {
				List<IndexSyncBlock> out = mongo.getSyncBlockIndex(c);
				return out;
			}
		}
		//collect sync blocks
		processImports(this._c);
		createBaseEnv(c);
		for(IndexSyncBlock s : output){
			mongo.add(s);
		}
		return output;
	}

	/**
	 * It connects to the mongodb and prepare the data in order to resolve the from which
	 * objects the method calls are coming from.
	 */
	private void processImports(ASTClass c) {
		for(ASTImport imp : c.getImports()){
			String pkg = imp.getPackagename();
			List<IndexData> d = mongo.getFromImport(pkg, true);
			if(d.size() > 0) imports.addAll(d);
		}
		//add myself as well
		String pkg = c.getPackageName() + "." + c.getName();
		List<IndexData> d = mongo.getFromImport(pkg, true);
		if(d.size() > 0) imports.addAll(d);
		//and my subs
		d = mongo.getFromImport(pkg + ".*", true);
		if(d.size() > 0) imports.addAll(d);
		if(c.getParent() != null){
			processImports(c.getParent());
		}
	}

	/**
	 * The following method creates the basic environment for a class.
	 * @param c Class to analyze
	 */
	@Override
	protected Env createBaseEnv(ASTClass c){
		super.createBaseEnv(c);
		//check method
		for (IASTMethod m : c.getMethods()) {
			lastMethodName = m.getName();
			signatureLastMethodName = new ArrayList<>();
			for(ASTVariable p : m.getParameters()){
				signatureLastMethodName.add(p.getType());
			}
			Env eMethod = new Env(base_env);
			eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
			super.analyze(m.getStms(), eMethod);
		}
		return base_env;
	}

	@Override
	protected void analyzeASTSynchronized(ASTSynchronized elm, Env env) {
		if(lastMethodName.equals("")) {
			//we are in static
			for(IASTMethod m : this._c.getMethods()){
				if(m instanceof ASTConstructor){
					lastMethodName = m.getName();
					signatureLastMethodName = new ArrayList<>();
					for(ASTVariable p : m.getParameters()){
						signatureLastMethodName.add(p.getType());
					}
					doJob(elm, env);
				}
			}
		} else {
			doJob(elm, env);
		}
	}

	private void doJob(ASTSynchronized elm, Env env){
		Pair<String,String> exprType = ResolveTypes.getSynchronizedExprType(imports, elm.getExpr().getExpression(), this._c, env);
		//System.out.println(exprType.getValue0() + " : " + exprType.getValue1());
		IndexSyncBlock sync = new IndexSyncBlock();
		sync.setPackageName(this._c.getPackageName());
		sync.setPath(this._c.getPath());
		sync.setName(this._c.getName());
		sync.setMethodName(lastMethodName);
		sync.setExpr(elm.getExpr().getCode());
		sync.setStart(elm.getStart());
		sync.setEnd(elm.getEnd());
		sync.setLine(elm.getLine());
		IndexEnv e_index = new IndexEnv(env);
		sync.setSyncVar( e_index.getVar(sync.getExpr()) );
		sync.setSignature(signatureLastMethodName);
		sync.setExprPkg(exprType.getValue0());
		sync.setExprType(exprType.getValue1());

		//is accessible from outside
		boolean startValue = (exprType.getValue0().equals("") && exprType.getValue1().equals("")); //workaround to check if is inherited!
		boolean[] flag = {startValue};
		//check if the expression of the current variable is possible to be used outside of the class
		// can be used only in two cases:
		// 1. The variable is in a return statement
		// 2. Appears in left hand side of an assignment
		// 2.1 Must not be an array (we change the value stored inside the var, not the variable itself)
		// 2.2 Must not be a new declaration
		ParseIM checkAccessibleFromOutside = new ParseIM() {
			private boolean checkIASTRE(IASTRE e, Env env){
				if(e instanceof ASTLiteral){
					if(((ASTLiteral) e).getValue().equals(sync.getExpr())){
						if(((ASTLiteral) e).getCode().endsWith("]")){ //does it work like that?
							//We should think about gettin' in touch with the concrete types of the program and do not abstract from them. At least arrays...
							//handle cases where we store smth inside an array
							return false;
						}
						return true;
					}
				}
				if(e instanceof ASTAttributeAccess){
					if(((ASTAttributeAccess) e).getAttributeName().equals(sync.getExpr())){
						if(((ASTAttributeAccess) e).getCode().endsWith("]")){ //does it work like that?
							//handle cases where we store smth inside an array
							return false;
						}
						return true;
					}
				}
				return false;
			}

			private boolean checkNew(IASTRE right, Env env) {
				return (right instanceof ASTNewObject);
			}

			@Override
			protected void analyzeASTRE(ASTRE r, Env env) {
				if(r != null && r.getExpression() != null)
					r.getExpression().visit(new DefualtASTREVisitor(){
						@Override
						public void enterASTAssignment(ASTAssignment elm) {
							IASTRE left = elm.getLeft();
							IASTRE right = elm.getRight();
							if(!flag[0]) {
								flag[0] = checkIASTRE(left, env) && !checkNew(right, env);
							}
						}
					});
			}

			@Override
			protected void analyzeASTReturn(ASTReturn elm, Env env) {
				ASTRE expr = elm.getExpr();
				if(expr != null){
					IASTRE e = expr.getExpression();
					if(!flag[0]) {
						flag[0] = checkIASTRE(e, env);
					}
				}
			}

			public void start(ASTClass _c){
				for(IASTMethod m : _c.getMethods()){
					analyzeMethod(m);
				}
			}
		};
		checkAccessibleFromOutside.start(_c);
		sync.setAccessibleFromOutside(flag[0]);
		output.add(sync);
	}
}