package IntermediateModelHelper.indexing;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import IntermediateModelHelper.indexing.structure.SyncMethodCall;
import IntermediateModelHelper.types.DataTreeType;
import IntermediateModelHelper.types.ResolveTypes;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTAttributeAccess;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The idea is to parse the method that we are currently looking through and collect all the information about
 * what are the method calls to synchronized method.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GenerateMethodSyncCallList extends ParseIM {

	ASTClass _class;
	List<IASTMethod> _methods = new ArrayList<>();
	List<SyncMethodCall> syncCalls = new ArrayList<>();
	MongoConnector mongo;
	Map<String, List<IndexMethod>> syncMethods = new HashMap<>();
	List<IndexData> imports = new ArrayList<>();
	String lastMethod;
	List<String> lastSignature = new ArrayList<>();

	/**
	 * Constructor.
	 * It creates the object and pre-process the {@link ASTClass} in input collecting from the database all the info
	 * from the import section.
	 * @param _class	Class under analysis
	 * @param _method	Method of the class under analysis
	 */
	public GenerateMethodSyncCallList(ASTClass _class, IASTMethod _method) {
		this._class = _class;
		this._methods.add(_method);
		mongo = MongoConnector.getInstance(MongoOptions.getInstance().getDbName());
		processImports();

	}

	/**
	 * Constructor.
	 * It creates the object and pre-process the {@link ASTClass} in input collecting from the database all the info
	 * from the import section.
	 * @param _class	Class under analysis
	 * @param _methods	List of Methods of the class under analysis
	 */
	public GenerateMethodSyncCallList(ASTClass _class, List<IASTMethod> _methods) {
		this._class = _class;
		this._methods = _methods;
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
			List<IndexData> current = mongo.getFromImport(pkg, true);
			if(current.size() > 0) imports.addAll(current);
			//System.out.println(Arrays.toString(d.toArray()));
			for(IndexData index : current){
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
		for(IASTMethod m : this._methods){
			lastMethod = m.getName();
			lastSignature.clear();
			for(ASTVariable p : m.getParameters()){
				lastSignature.add(p.getType());
			}
			super.analyzeMethod(m);
		}
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
		String inMethod = lastMethod;
		List<String> inSignature = new ArrayList<>();
		inSignature.addAll(lastSignature);
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
									ResolveTypes.getImportPkgFromType(imports, env.getExprType(p))
							)

					);
				}
				String pkg2 = _class.getPackageName();
				if(expr == null){
					//local call - we can skip constructors because they are never sync
					for(IASTMethod m : _class.getMethods()){
						if(m instanceof ASTMethod && m.getName().equals(methodCalled)){
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
									//local call
									syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, m.getSignature(), r, methodPars, inMethod, inSignature));
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
								List<String> signature = new ArrayList<String>();
								for(int i = 0, max = m.getParameters().size(); i < max; i++){
									methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
									signature.add(m.getParameters().get(i).getType());
								}
								syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(),  methodCalled, signature, r, methodPars, inMethod, inSignature));
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
										List<String> signature = new ArrayList<String>();
										for(int i = 0, max = m.getParameters().size(); i < max; i++){
											methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
											signature.add(m.getParameters().get(i).getType());
										}
										syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, signature, r, methodPars, inMethod, inSignature));
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
										syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, m.getSignature(), r, methodPars, inMethod, inSignature));
									}
								}
							}
						}
					}
				}
				if(expr instanceof ASTMethodCall){
					String type = ResolveTypes.getTypeMethodCall(imports, _class, (ASTMethodCall) expr, env);
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
									List<String> signature = new ArrayList<String>();
									for(int i = 0, max = m.getParameters().size(); i < max; i++){
										methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
										signature.add(m.getParameters().get(i).getType());
									}
									syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, signature, r, actual_pars, inMethod, inSignature));
								}
							}
						}
					}
				}
			}
		});
	}



}
