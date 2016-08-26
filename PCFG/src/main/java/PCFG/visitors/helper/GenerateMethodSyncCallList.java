package PCFG.visitors.helper;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.DataTreeType;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
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
import org.javatuples.Pair;

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

	ASTClass _class;
	IASTMethod _method;
	List<SyncMethodCall> syncCalls = new ArrayList<>();
	MongoConnector mongo;
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
		mongo = MongoConnector.getInstance(MongoOptions.getInstance().getDbName());
		processImports();

	}

	/**
	 * It connects to the mongodb and prepare the data in order to resolve the from which
	 * objects the method calls are coming from.
	 */
	private void processImports() {
		for(ASTImport imp : this._class.getImports()){
			String pkg = imp.getPackagename();
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

	private boolean containsMethod(String name, List<Pair<String,String>> parsType, List<IndexMethod> methods){
		for(IndexMethod m : methods){
			if(m.equalBySignature(name, parsType))
				return true;
		}
		return false;
	}
	private IndexMethod getMethod(String name, List<Pair<String,String>> parsType, List<IndexMethod> methods){
		for(IndexMethod m : methods){
			if(m.equalBySignature(name, parsType))
				return m;
		}
		return null;
	}

	/**
	 * Run the computation of the calls to a synchronized method
	 * @return	List of {@link SyncMethodCall}
	 */
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
		if(r == null || r.getExpression() == null){
			return;
		}
		r.getExpression().visit(new DefualtASTREVisitor(){
			@Override
			public void enterASTMethodCall(ASTMethodCall elm) {
				String methodCalled = elm.getMethodName();
				IASTRE expr = elm.getExprCallee();
				List<Pair<String,String>> actual_pars = new ArrayList<Pair<String,String>>();
				for(IASTRE p : elm.getParameters()){
					actual_pars.add(
							new Pair<String, String>(
									env.getExprType(p),
									getImportPkgFromType(env.getExprType(p))
							)

					);
				}
				String pkg2 = _class.getPackageName();
				if(expr == null){
					//local call - we can skip constructors because they are never sync
					for(IASTMethod m : _class.getMethods()){
						if(m instanceof ASTMethod){
							ASTMethod method = ((ASTMethod) m);
							if(method.isSyncronized()){
								boolean flag = true;
								if(actual_pars.size() == m.getParameters().size()){
									for(int i = 0, max = actual_pars.size(); i < max; i++){
										String pkg1 = actual_pars.get(i).getValue1();
										if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, pkg2)){
											flag = false;
										}
									}
								} else {
									flag = false;
								}
								if(flag) {
									List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
									for(int i = 0, max = m.getParameters().size(); i < max; i++){
										methodPars.add(new Pair<>( m.getParameters().get(i).getType(), _class.getPackageName() ));
									}
									syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, r, methodPars));
								}
							}
						}
					}
				}
				if(expr instanceof ASTAttributeAccess){
					String attribute = ((ASTAttributeAccess) expr).getAttributeName();
					if(syncMethods.containsKey(attribute)){
						//exists in the list
						List<IndexMethod> methods = syncMethods.get(attribute);
						if(containsMethod(methodCalled, actual_pars, methods)){
							//also the call is in the list -> we gotta a match
							IndexMethod m = getMethod(methodCalled, actual_pars, methods);
							boolean flag = true;
							if(actual_pars.size() == m.getParameters().size()){
								for(int i = 0, max = actual_pars.size(); i < max; i++){
									String pkg1 = actual_pars.get(i).getValue1();
									if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, m.getPackageName())){
										flag = false;
									}
								}
							} else {
								flag = false;
							}
							if(flag) {
								List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
								for(int i = 0, max = m.getParameters().size(); i < max; i++){
									methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
								}
								syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, r, methodPars));
							}
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
								if(containsMethod(methodCalled, actual_pars, methods)){
									//also the call is in the list -> we gotta a match
									IndexMethod m = getMethod(methodCalled, actual_pars, methods);
									boolean flag = true;
									if(actual_pars.size() == m.getParameters().size()){
										for(int i = 0, max = actual_pars.size(); i < max; i++){
											String pkg1 = actual_pars.get(i).getValue1();
											if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, m.getPackageName())){
												flag = false;
											}
										}
									} else {
										flag = false;
									}
									if(flag) {
										List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
										for(int i = 0, max = m.getParameters().size(); i < max; i++){
											methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
										}
										syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, r, methodPars));
									}
								}
							}
						}
					} else { //if is this. check locally
						for(IASTMethod m : _class.getMethods()){
							if(m instanceof ASTMethod){
								ASTMethod method = ((ASTMethod) m);
								if(method.isSyncronized()){
									boolean flag = true;
									if(actual_pars.size() == m.getParameters().size()){
										for(int i = 0, max = actual_pars.size(); i < max; i++){
											String pkg1 = actual_pars.get(i).getValue1();
											if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, pkg2)){
												flag = false;
											}
										}
									} else {
										flag = false;
									}
									if(flag) {
										List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
										for(int i = 0, max = m.getParameters().size(); i < max; i++){
											methodPars.add(new Pair<>( m.getParameters().get(i).getType(), _class.getPackageName() ));
										}
										syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, r, methodPars));
									}
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
							if(containsMethod(methodCalled, actual_pars, methods)){
								//also the call is in the list -> we gotta a match
								IndexMethod m = getMethod(methodCalled, actual_pars, methods);
								boolean flag = true;
								if(actual_pars.size() == m.getParameters().size()){
									for(int i = 0, max = actual_pars.size(); i < max; i++){
										String pkg1 = actual_pars.get(i).getValue1();
										if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, m.getPackageName())){
											flag = false;
										}
									}
								} else {
									flag = false;
								}
								if(flag) {
									List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
									for(int i = 0, max = m.getParameters().size(); i < max; i++){
										methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
									}
									syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, r, actual_pars));
								}
							}
						}
					}
				}
			}
		});
	}

	private String getImportPkgFromType(String s) {
		String out = null;
		for(IndexData i : imports){
			DataTreeType tImp = null;
			try {
				tImp = new DataTreeType(i.getClassName(), i.getClassPackage());
				if(tImp.isTypeCompatible(s)){
					out = i.getClassPackage();
				}
			} catch (Exception e) {
				continue;
			}
		}
		return out;
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
