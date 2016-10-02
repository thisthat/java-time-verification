package IntermediateModelHelper.types;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTAttribute;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTImport;
import intermediateModel.structure.expression.ASTAttributeAccess;
import intermediateModel.structure.expression.ASTLiteral;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.internal.core.index.Index;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ResolveTypes {

	public static IndexData getPackageFromImports(List<IndexData> imports, String type){
		for(IndexData imp : imports){
			if(imp.getClassName().equals(type))
				return imp;
		}
		//we can import a subclass for importing as well the parent one .-. apparently
		for(IndexData imp : imports){
			if(type == null){
				continue;
			}
			if(imp.getClassPackage().endsWith(type))
				return imp;
		}
		return null;
	}

	public static String getImportPkgFromType(List<IndexData> imports, String s) {
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
	 * Possible cases:
	 * <ul>
	 *     <li><b>Attribute access ( Class.method() )</b>: search in the imports the class that has that method</li>
	 *     <li><b>Variable ( var.method() )</b>: <ul>
	 *         <li><b>this</b>: search the method in the current class</li>
	 *         <li><b>variable</b>: get the variable type from the {@link Env} and get the method from its class</li>
	 *     </ul></li>
	 *     <li><b>Method call ( methodn()._.method1().method0() )</b>: Resolve the type of the last call recursively and then search for the method definition in the class of the last type</li>
	 * </ul>
	 * @param expr	Expression which contains the method call to resolve the type
	 * @return		Name of the type or null if we cannot solve it
	 */
	public static String getTypeMethodCall(List<IndexData> imports, ASTClass _class, ASTMethodCall expr, Env e){
		IASTRE calee = expr.getExprCallee();
		if(calee == null){
			//local method call
			calee = new ASTLiteral(-1,-1, "this");
		}
		String methodCalled = expr.getMethodName();
		List<Pair<String,String>> actual_pars = new ArrayList<Pair<String,String>>();
		for(IASTRE p : expr.getParameters()){
			String exprType;
			if(p instanceof ASTMethodCall){
				exprType = ResolveTypes.getTypeMethodCall(imports, _class, (ASTMethodCall) p, e);
			} else {
				exprType = e.getExprType(p);
			}
			actual_pars.add(
					new Pair<String, String>(
							exprType,
							ResolveTypes.getImportPkgFromType(imports, e.getExprType(p))
					)

			);
		}
		if(calee instanceof ASTAttributeAccess){
			final String[] varTypeHelper = new String[1];
			((ASTAttributeAccess) calee).getVariableName().visit(new DefualtASTREVisitor(){
				@Override
				public void enterASTLiteral(ASTLiteral elm) {
					varTypeHelper[0] = elm.getValue();
				}
			});
			String varType = varTypeHelper[0];
			IndexData _class_pkg = ResolveTypes.getPackageFromImports(imports,varType);
			if(_class_pkg != null){
				//we have smth to work with
				for(IndexMethod m : _class_pkg.getListOfMethods()){
					if(m.equalBySignature(methodCalled, actual_pars)){
						return m.getReturnType();
					}
				}
			}
		}
		if(calee instanceof ASTLiteral){
			String varName = ((ASTLiteral) calee).getValue();
			if(varName.equals("this")){
				//local search
				return localSearch(_class, methodCalled, actual_pars);
			} else {
				//get type in env
				IASTVar var = e.getVar(varName);
				if(var != null){
					//exist smth in the env (should be always the case)
					IndexData _classImport = ResolveTypes.getPackageFromImports(imports,var.getType());
					if(_classImport != null){
						//we have smth to work with
						for(IndexMethod m : _classImport.getListOfMethods()){
							if(m.equalBySignature(methodCalled, actual_pars)){
								return m.getReturnType();
							}
						}
					}
				} else {
					//static call?
					IndexData _classImport = ResolveTypes.getPackageFromImports(imports, varName);
					if(_classImport != null){
						for(IndexMethod m : _classImport.getListOfMethods()){
							if(m.equalBySignature(methodCalled, actual_pars)){
								return m.getReturnType();
							}
						}
					}
				}
			}
		}
		if(calee instanceof ASTMethodCall){
			//recursive call
			String previousCallReturn = getTypeMethodCall(imports, _class, ((ASTMethodCall) calee), e);
			IndexData _classImport = ResolveTypes.getPackageFromImports(imports,previousCallReturn);
			if(_classImport != null){
				//we have smth to work with
				for(IndexMethod m : _classImport.getListOfMethods()){
					if(m.equalBySignature(methodCalled, actual_pars)){
						return m.getReturnType();
					}
				}
			} else {
				MongoConnector mongo = MongoConnector.getInstance();
				for(IndexData imp : imports){ //it is possible that it is in the same package of the previous method call
					List<IndexData> src = mongo.getIndex(previousCallReturn, imp.getClassPackage());
					if(src.size() > 0){
						for(IndexData d : src){
							for(IndexMethod m :d.getListOfMethods()){
								if(m.equalBySignature(methodCalled, actual_pars)){
									return m.getReturnType();
								}
							}
						}
					}
				}
				//could be a local call then
				for (IASTMethod m : _class.getMethods()) {
					if (m.equalsBySignature(methodCalled, actual_pars)) {
						return m.getReturnType();
					}
				}
			}
		}
		return "Object";
	}

	private static String localSearch(ASTClass _class, String methodCalled, List<Pair<String,String>> actual_pars){
		for(IASTMethod m : _class.getMethods()){
			if(m.equalsBySignature(methodCalled, actual_pars)){
				return m.getReturnType();
			}
		}
		if(_class.getParent() != null)
			return localSearch(_class.getParent(), methodCalled, actual_pars);
		else
			return "";
	}

	public static Pair<String,String> getSynchronizedExprType(List<IndexData> imports, IASTRE expr, ASTClass c, Env e){
		//5 cases: this, var, methodCall(), Smth.class, Smth.attribute
		Pair<String,String> out = new Pair<>("","");
		//cases: this, var, Smth.class, ClassName.this
		if(expr instanceof ASTLiteral){
			String val = ((ASTLiteral) expr).getValue();
			if(val.equals("this") || val.endsWith(".this")) {
				out = new Pair<>(c.getPackageName(), c.getName());
			}
			else if(val.endsWith(".class")){
				String class_name = val.substring(0, val.indexOf(".class"));
				if(class_name.equals(c.getName())){
					out = new Pair<>(c.getPackageName(), c.getName());
				} else {
					IndexData d = getPackageFromImports(imports, class_name);
					if(d == null){
						//if d is null then it is a normal object
						out = new Pair<>("java.lang", class_name);
					} else {
						out = new Pair<>(d.getClassPackage(), d.getClassName());
					}
				}
			}
			else {
				//it is a variable
				IASTVar type = e.getVar(val);
				if(type == null){
					System.err.println("[Var] Should be not the case! Resolving sync expr for class:" + c.getPath());
					System.err.println("@line:" + ((ASTLiteral) expr).getLine());
				} else {
					IndexData d = getPackageFromImports(imports, type.getType());
					if(d == null){
						//if d is null then it is a normal object
						out = new Pair<>("java.lang", type.getType());
					} else {
						out = new Pair<>(d.getClassPackage(), d.getClassName());
					}
				}
			}
		}
		//cases: Smth.attribute
		if(expr instanceof ASTAttributeAccess){
			IASTRE var = ((ASTAttributeAccess) expr).getVariableName();
			String attribute = ((ASTAttributeAccess) expr).getAttributeName();
			if (var instanceof ASTLiteral) {
				String varName = ((ASTLiteral) var).getValue();
				if(varName.equals("this")){
					varName = attribute;
				}
				IASTVar type = e.getVar(varName);
				if (type == null) {
					System.err.println("[Attribute] Should be not the case! Resolving sync expr for class:" + c.getPath());
					System.err.println("@line:" + ((ASTAttributeAccess) expr).getLine() );
				} else {
					IndexData d = getPackageFromImports(imports, type.getType());
					if (d == null) {
						//if d is null then it is a standard obj
						out = new Pair<>("java.lang", type.getType());
					} else {
						List<ASTClass> classes = JDTVisitor.parse(d.getPath());
						for(ASTClass cc : classes){
							if(cc.getName().equals(type.getType())){
								for(ASTAttribute a : cc.getAttributes()){
									if(a.getName().equals(attribute)){
										out = getType(a.getType(),cc);
									}
								}
							}
						}

					}
				}
			}
		}
		//cases: methodCall()
		if(expr instanceof ASTMethodCall){
			String type = getTypeMethodCall(imports, c, (ASTMethodCall) expr, e);
			out = new Pair<>("",type);
		}
		return out;
	}

	public static Pair<String,String> getType(String type, ASTClass c){
		List<IndexData> imports = new ArrayList<>();
		Pair<String,String> out = new Pair<>("","");
		for(ASTImport i : c.getImports()){
			imports.addAll(MongoConnector.getInstance().getFromImport(i.getPackagename(), false));
		}
		IndexData d = getPackageFromImports(imports, type);
		if(d == null){
			//if d is null then it is a normal object
			out = new Pair<>("java.lang", type);
		} else {
			out = new Pair<>(d.getClassPackage(), d.getClassName());
		}
		return out;
	}
}
