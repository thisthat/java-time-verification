package PCFG.visitors.helper;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTImport;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTAttributeAccess;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.ParseIM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * With the following class we help the construction of the {@link PCFG.structure.SyncEdge}.
 * The idea is to parse the method that we are currently looking through and collect all the information about
 * what are the method calls to synchronized method.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GenerateMethodSyncCallList extends ParseIM {

	//TODO: create a common sharing structure with configuration
	public static final String VUZE = "/Users/giovanni/repository/java-xal/intermediate-model/src/test/resources/vuze";
	public static final String DB_NAME = "vuze";

	ASTClass _class;
	IASTMethod _method;
	List<SyncMethodCall> syncCalls = new ArrayList<>();
	MongoConnector mongo = MongoConnector.getInstance(DB_NAME);
	Map<String, List<IndexMethod>> syncMethods = new HashMap<>();
	List<IndexData> imports = new ArrayList<>();

	/**
	 * Constructor.
	 * It creates the object and pre-process the {@link ASTClass} in input collecting from the database all the info
	 * from the import section.
	 * @param _class	Class under analysis
	 * @param _method	Method of the class under analysis
	 */
	public GenerateMethodSyncCallList(ASTClass _class, IASTMethod _method) {
		this._class = _class;
		this._method = _method;
		processImports();
	}

	/**
	 * It connects to the mongodb and prepare the data in order to resolve the from which
	 * objects the method calls are coming from.
	 */
	private void processImports() {
		for(ASTImport imp : this._class.getImports()){
			String pkg = imp.getPackagename();
			System.out.println(pkg);
			imports = mongo.getFromImport(pkg);
			//System.out.println(Arrays.toString(d.toArray()));
			for(IndexData index : imports){
				String objName = index.getClassName();
				List<IndexMethod> names = new ArrayList<>();
				for(IndexMethod m : index.getListOfSyncMethods()){
					names.add(m);
				}
				syncMethods.put(objName, names);
			}
		}
	}

	private boolean containsMethod(String name, List<IndexMethod> methods){
		for(IndexMethod m : methods){
			if(m.getName().equals(name))
				return true;
		}
		return false;
	}
	private IndexMethod getMethod(String name, List<IndexMethod> methods){
		for(IndexMethod m : methods){
			if(m.getName().equals(name))
				return m;
		}
		return null;
	}


	public List<SyncMethodCall> calculateSyncCallList(){
		syncCalls.clear();
		super.createBaseEnv(_class);
		super.analyzeMethod(this._method);
		return syncCalls;
	}


	/**
	 * We are only interested in ASTRE
	 * <ul>
	 *     <li><b>Attribute Access</b>: Check if the attribute name exists in the list of Sync methods and as well the method call in its list.</li>
	 *     <li><b>Variable</b>: Get the type of the variable, with it check if is in the list of Sync methods and as well the method call in its list.</li>
	 *     <li><b>Multiple Method call</b>: Get the type of the return of the previous call, with it check if is in the list of Sync methods and as well
	 *     									the method call in its list. Moreover, we do not need to process recursively the check because it does it for us the visitor.</li>
	 *     <li><b>null</b>: Check if the call to a local class method is sync or not.</li>
	 * </ul>
	 * @param r 	Statement
	 * @param env   Environment
	 */
	@Override
	protected void analyzeASTRE(ASTRE r, Env env) {
		r.getExpression().visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				String methodCalled = elm.getMethodName();
				IASTRE expr = elm.getExprCallee();
				if(expr == null){
					//local call - we can skip constructors because they are never sync
					for(IASTMethod m : _class.getMethods()){
						if(m instanceof ASTMethod){
							if(((ASTMethod) m).isSyncronized()){
								syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, r));
							}
						}
					}
				}
				if(expr instanceof ASTAttributeAccess){
					String attribute = ((ASTAttributeAccess) expr).getAttributeName();
					if(syncMethods.containsKey(attribute)){
						//exists in the list
						List<IndexMethod> methods = syncMethods.get(attribute);
						if(containsMethod(methodCalled, methods)){
							//also the call is in the list -> we gotta a match
							IndexMethod m = getMethod(methodCalled, methods);
							syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getType(), methodCalled, r));
						}
					}
				}
				if(expr instanceof ASTLiteral){
					String varName = ((ASTLiteral) expr).getValue();
					if(!varName.equals("this")){
						IASTVar var = env.getVar(varName);
						if(var != null){ //we have smth in the env (should be always the case)
							String varType = var.getType();
							if(syncMethods.containsKey(varType)){
								//exists in the list
								List<IndexMethod> methods = syncMethods.get(varType);
								if(containsMethod(methodCalled, methods)){
									//also the call is in the list -> we gotta a match
									IndexMethod m = getMethod(methodCalled, methods);
									syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getType(), methodCalled, r));
								}
							}
						}
					} else { //if is this. check locally
						for(IASTMethod m : _class.getMethods()){
							if(m instanceof ASTMethod){
								if(((ASTMethod) m).isSyncronized()){
									syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, r));
								}
							}
						}
					}
				}
				if(expr instanceof ASTMethodCall){
					String type = getTypeMethodCall((ASTMethodCall) expr, env);
					if(type != null){
						//we have a type
						if(syncMethods.containsKey(type)){
							//exists in the list
							List<IndexMethod> methods = syncMethods.get(type);
							if(containsMethod(methodCalled, methods)){
								//also the call is in the list -> we gotta a match
								IndexMethod m = getMethod(methodCalled, methods);
								syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getType(), methodCalled, r));
							}
						}
					}
				}
			}
		});
	}

	/**
	 * Resolve the type of a method call.
	 * @param expr	Expression which contains the method call to resolve the type
	 * @return		Name of the type or null if we cannot solve it
	 */
	private String getTypeMethodCall(ASTMethodCall expr, Env e){
		IASTRE calee = expr.getExprCallee();
		String methodCalled = expr.getMethodName();
		if(calee instanceof ASTAttributeAccess){
			final String[] varTypeHelper = new String[1];
			((ASTAttributeAccess) calee).getVariableName().visit(new DefualtASTREVisitor(){
				@Override
				public void enterASTLiteral(ASTLiteral elm) {
					varTypeHelper[0] = elm.getValue();
				}
			});
			String varType = varTypeHelper[0];
			IndexData _class = searchInImports(varType);
			if(_class != null){
				//we have smth to work with
				for(IndexMethod m : _class.getListOfMethods()){
					if(m.getName().equals(methodCalled)){
						return m.getReturnType();
					}
				}
			}
		}
		if(calee instanceof ASTLiteral){
			String varName = ((ASTLiteral) calee).getValue();
			if(varName.equals("this")){
				//local search
				for(IASTMethod m : _class.getMethods()){
					if(m.getName().equals(methodCalled)){
						return m.getReturnType();
					}
				}
			} else {
				//get type in env
				IASTVar var = e.getVar(varName);
				if(var != null){
					//exist smth in the env (should be always the case)
					IndexData _classImport = searchInImports(var.getType());
					if(_classImport != null){
						//we have smth to work with
						for(IndexMethod m : _classImport.getListOfMethods()){
							if(m.getName().equals(methodCalled)){
								return m.getReturnType();
							}
						}
					}
				}
			}
		}
		if(calee instanceof ASTMethodCall){
			//recursive call
			String previousCallReturn = getTypeMethodCall(((ASTMethodCall) calee), e);
			IndexData _classImport = searchInImports(previousCallReturn);
			if(_classImport != null){
				//we have smth to work with
				for(IndexMethod m : _classImport.getListOfMethods()){
					if(m.getName().equals(methodCalled)){
						return m.getReturnType();
					}
				}
			} else {
				//could be a local call then
				for(IASTMethod m : _class.getMethods()){
					if(m.getName().equals(methodCalled)){
						return m.getReturnType();
					}
				}
			}
		}
		return null;
	}

	private IndexData searchInImports(String type){
		for(IndexData imp : imports){
			if(imp.getClassName().equals(type))
				return imp;
		}
		return null;
	}
}
