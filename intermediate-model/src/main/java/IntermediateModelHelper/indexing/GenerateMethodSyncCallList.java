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
	ASTClass _lastClass;
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
		this._lastClass = _class;
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
				processImportClass(index, objName);
			}
		}
		//plus files in my package
		String pkg = this._class.getPackageName() + ".*";
		List<IndexData> current = mongo.getFromImport(pkg, true);
		if(current.size() > 0) imports.addAll(current);
		//System.out.println(Arrays.toString(d.toArray()));
		for(IndexData index : current){
			String objName = index.getClassName();
			processImportClass(index,objName);
		}

	}

	private void processImportClass(IndexData index, String objName){
		List<IndexMethod> names = new ArrayList<>();
		for(IndexMethod m : index.getListOfSyncMethods()){
			names.add(m);
		}
		syncMethods.put(objName, names);
		//now search on parent
		if(!index.getExtendedType().equals("Object")){
			String type = index.getExtendedType();
			for(String imp : index.getImports()){
				if(imp.endsWith(type)){
					List<IndexData> parentIndex = mongo.getIndex(imp, type);
					for(IndexData pIdx : parentIndex){
						processImportClass(pIdx, objName);
					}
				}
			}
		}
	}

	private IndexData getFromImport(String type) {
		for(IndexData i : imports){
			if(i.getClassName().equals(type))
				return i;
		}
		return null;
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
		String inMethodPkg = _class.getPackageName();
		String inMethodClass = _class.getName();
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
					localCall(methodCalled, actual_pars, pkg2, r, inMethod, inSignature, inMethodPkg, inMethodClass);
				}
				if(expr instanceof ASTAttributeAccess){
					String attribute = ((ASTAttributeAccess) expr).getAttributeName();
					searchInParent(attribute,actual_pars, methodCalled, r, inMethod, inSignature, inMethodPkg, inMethodClass);
				}
				if(expr instanceof ASTLiteral){
					String varName = ((ASTLiteral) expr).getValue();
					literalAccess(methodCalled, actual_pars, pkg2, r, inMethod, inSignature, inMethodPkg, inMethodClass, varName, env);
				}
				if(expr instanceof ASTMethodCall){
					String type = ResolveTypes.getTypeMethodCall(imports, _class, (ASTMethodCall) expr, env);
					methodAccess(methodCalled, actual_pars, type, r, inMethod, inSignature, inMethodPkg, inMethodClass);
				}
			}
		});
	}

	private String getParentType(String type){
		for(int i = 0; i < imports.size(); i++){
			if(imports.get(i).getClassName().equals(type)){
				return imports.get(i).getExtendedType();
			}
		}
		return "Object";
	}

	private void localCall(String methodCalled, List<Pair<String,String>> actual_pars, String pkgClass, ASTRE r, String inMethod, List<String> inSignature, String inMethodPkg, String inMethodClass){
		//local call - we can skip constructors because they are never sync
		boolean found = false;
		for(IASTMethod m : _class.getMethods()){
			if(m instanceof ASTMethod && m.getName().equals(methodCalled)){
				found = true;
				ASTMethod method = ((ASTMethod) m);
				if(method.isSyncronized()){
					boolean flag = true;
					if(actual_pars.size() == m.getParameters().size()){
						for(int i = 0, max = actual_pars.size(); i < max; i++){
							String pkg1 = actual_pars.get(i).getValue1();
							if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, pkgClass)){
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
						syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, m.getSignature(), r, methodPars, inMethod, inSignature, inMethodPkg, inMethodClass));
					}
				}
			}
		}
		//should we consider also the parent one? I suppose jup we have to do ;)
		if(!found){
			searchInParent(this._class.getExtendClass(), actual_pars, methodCalled, r, inMethod, inSignature, inMethodPkg, inMethodClass);
		}
	}

	private void literalAccess(String methodCalled, List<Pair<String,String>> actual_pars, String pkgClass, ASTRE r, String inMethod, List<String> inSignature, String inMethodPkg, String inMethodClass, String varName, Env env){
		if(varName.equals("this")){ //if is this. check locally
			for(IASTMethod m : _class.getMethods()){
				if(m instanceof ASTMethod){
					ASTMethod method = ((ASTMethod) m);
					if(method.isSyncronized() && method.getName().equals(methodCalled)){
						boolean flag = true;
						if(actual_pars.size() == m.getParameters().size()){
							for(int i = 0, max = actual_pars.size(); i < max; i++){
								String pkg1 = actual_pars.get(i).getValue1();
								if(!DataTreeType.checkEqualsTypes(actual_pars.get(i).getValue0(), m.getParameters().get(i).getType(), pkg1, pkgClass)){
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
							syncCalls.add(new SyncMethodCall(_class.getPackageName(), _class.getName(), methodCalled, m.getSignature(), r, methodPars, inMethod, inSignature, inMethodPkg, inMethodClass));
						}
					}
				}
			}
		} else if(varName.equals("super")) {
			//search in parent class
			String parentObjType = this._class.getExtendClass();
			while(!parentObjType.equals("Object")) {
				List<IndexMethod> methods = syncMethods.get(parentObjType);
				//if it is null then it extends some class in a framework that we cannot consider
				if (methods != null && containsMethod(methodCalled, actual_pars, methods)) {
					//also the call is in the list -> we gotta a match
					IndexMethod m = getMethod(methodCalled, actual_pars, methods);
					if (m != null) {
						List<Pair<String, String>> methodPars = new ArrayList<Pair<String, String>>();
						List<String> signature = new ArrayList<String>();
						for (int i = 0, max = m.getParameters().size(); i < max; i++) {
							methodPars.add(new Pair<>(m.getParameters().get(i).getType(), m.getPackageName()));
							signature.add(m.getParameters().get(i).getType());
						}
						syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, signature, r, methodPars, inMethod, inSignature, inMethodPkg, inMethodClass));
					}
					parentObjType = "Object";
				} else {
					IndexData parent = getFromImport(parentObjType);
					if (parent != null) {
						parentObjType = parent.getExtendedType();
					} else {
						parentObjType = "Object";
					}
				}
			}
		} else { //search from the env
			IASTVar var = env.getVar(varName);
			if(var != null){ //we have smth in the env (should be always the case)
				String varType = var.getType();
				boolean found = false;
				if(syncMethods.containsKey(varType)){
					//exists in the list
					List<IndexMethod> methods = syncMethods.get(varType);
					if(containsMethod(methodCalled, actual_pars, methods)){
						found = true;
						//also the call is in the list -> we gotta a match
						IndexMethod m = getMethod(methodCalled, actual_pars, methods);
						if(m != null) {
							List<Pair<String,String>> methodPars = new ArrayList<Pair<String, String>>();
							List<String> signature = new ArrayList<String>();
							for(int i = 0, max = m.getParameters().size(); i < max; i++){
								methodPars.add(new Pair<>( m.getParameters().get(i).getType(), m.getPackageName() ));
								signature.add(m.getParameters().get(i).getType());
							}
							syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(), methodCalled, signature, r, methodPars, inMethod, inSignature, inMethodPkg, inMethodClass));
						}
					}
				}
				if(!found){
					searchInParent(varType, actual_pars, methodCalled, r, inMethod, inSignature, inMethodPkg, inMethodClass);
				}
			}
		}
	}

	private void methodAccess(String methodCalled, List<Pair<String,String>> actual_pars, String type, ASTRE r, String inMethod, List<String> inSignature, String inMethodPkg, String inMethodClass){
		if(type != null){
			searchInParent(type, actual_pars, methodCalled, r, inMethod, inSignature, inMethodPkg, inMethodClass);
		}
	}

	private void searchInParent(String parentType, List<Pair<String,String>> actual_pars, String methodCalled, ASTRE r, String inMethod, List<String> inSignature, String inMethodPkg, String inMethodClass ){
		boolean found = false;
		while(!found && !parentType.equals("Object")){
			if(syncMethods.containsKey(parentType)) {
				List<IndexMethod> methods = syncMethods.get(parentType);
				if(containsMethod(methodCalled, actual_pars, methods)){
					found = true;
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
						syncCalls.add(new SyncMethodCall(m.getPackageName(), m.getFromClass(),  methodCalled, signature, r, methodPars, inMethod, inSignature, inMethodPkg, inMethodClass));
					}
				} else {
					//search on the parent again
					parentType = getParentType(parentType);
				}
			} else {
				parentType = "Object";
			}
		}
	}
}
