package PCFG.converter.xal;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTVar;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTImport;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.ASTAttributeAccess;
import IntermediateModel.structure.expression.ASTCast;
import IntermediateModel.structure.expression.ASTLiteral;
import IntermediateModel.structure.expression.ASTMethodCall;
import IntermediateModel.visitors.DefualtASTREVisitor;
import IntermediateModel.visitors.interfaces.ParseIM;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import IntermediateModelHelper.types.ResolveTypes;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giovanni on 16/02/2017.
 */
class FindMethodCall extends ParseIM {
    MongoConnector mongo;
    Map<String, List<IndexMethod>> methods = new HashMap<>();
    List<IndexData> imports = new ArrayList<>();
    List<Triplet<String,String, ASTRE>> methodCalls = new ArrayList<>();

    public FindMethodCall(ASTClass _class) {
        super(_class);
        mongo = MongoConnector.getInstance(MongoOptions.getInstance().getDbName());
        processImports();
    }

    /**
     * Run the computation of the calls to a synchronized method
     * @return	List
     */
    public List<Triplet<String,String, ASTRE>> getMethodCalls(){
        methodCalls.clear();
        super.createBaseEnv(_class);
        for(IASTMethod m : this._class.getMethods()){
            super.analyzeMethod(m);
        }
        return methodCalls;
    }

    /**
     * It connects to the mongodb and prepare the data in order to resolve the from which
     * objects the method calls are coming from.
     */
    private void processImports() {
        for(ASTImport imp : this._class.getImports()){
            String pkg = imp.getPackagename();
            List<IndexData> current = mongo.getFromImport(pkg);
            if(current.size() > 0) imports.addAll(current);
            //System.out.println(Arrays.toString(d.toArray()));
            for(IndexData index : current){
                String objName = index.getClassName();
                processImportClass(index, objName);
            }
            if(Character.isUpperCase(pkg.substring(pkg.lastIndexOf(".") + 1).charAt(0))){
                pkg = pkg + ".*";
                current = mongo.getFromImport(pkg);
                if(current.size() > 0) imports.addAll(current);
                //System.out.println(Arrays.toString(d.toArray()));
                for(IndexData index : current){
                    String objName = index.getClassName();
                    processImportClass(index, objName);
                }
            }
        }
        //plus files in my package
        String pkg = this._class.getRealPackageName() + ".*";
        List<IndexData> current = mongo.getFromImport(pkg);
        if(current.size() > 0) imports.addAll(current);
        //System.out.println(Arrays.toString(d.toArray()));
        for(IndexData index : current){
            String objName = index.getClassName();
            processImportClass(index,objName);
        }
    }

    private void processImportClass(IndexData index, String objName){
        List<IndexMethod> names = new ArrayList<>();
        for(IndexMethod m : index.getListOfMethods()){
            names.add(m);
        }
        if(methods.containsKey(objName)){
            names.addAll(methods.get(objName));
        }
        methods.put(objName, names);
        //now search on parent
        if(!index.getExtendedType().equals("Object")){
            String type = index.getExtendedType();
            boolean found = false;
            for(String imp : index.getImports()){
                if(imp.endsWith(type)){
                    found = true;
                    List<IndexData> parentIndex = mongo.getIndex(type, imp);
                    for(IndexData pIdx : parentIndex){
                        processImportClass(pIdx, objName);
                        processImportClass(pIdx, pIdx.getClassName());
                    }
                }
            }
            if(!found){
                List<IndexData> parentIndex = mongo.getIndex(type, index.getClassPackage());
                for(IndexData pIdx : parentIndex){
                    processImportClass(pIdx, objName);
                    processImportClass(pIdx, pIdx.getClassName());
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

    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        if(r == null || r.getExpression() == null){
            return;
        }
		/*if(r.getLine() == 147){
			System.out.println("BRK");
		} else { return; }*/
        r.getExpression().visit(new DefualtASTREVisitor(){

            @Override
            public void enterASTMethodCall(ASTMethodCall elm) {
                String methodCalled = elm.getMethodName();
                IASTRE expr = elm.getExprCallee();
                List<Pair<String,String>> actual_pars = new ArrayList<Pair<String,String>>();
                for(IASTRE p : elm.getParameters()){
                    Pair<String,String> exprType;
                    if(p instanceof ASTMethodCall){
                        exprType = ResolveTypes.getTypeMethodCall(imports, _class, (ASTMethodCall) p, env);
                    } else {
                        String t = env.getExprType(p, _class);
                        if(t == null){
                            t = "null";
                        }
                        if(t.equals("this")) { t = _class.getName(); }
                        if(t.endsWith(".this")) { t = t.substring(0, t.lastIndexOf(".")); }
                        exprType = new Pair<>(t, ResolveTypes.getExactImportPkgFromType(imports, t) );
                    }
                    actual_pars.add(exprType);
                }
                String pkg2 = _class.getPackageName();
                if(expr == null){
                    localCall(methodCalled, actual_pars, pkg2, r);
                }
                if(expr instanceof ASTAttributeAccess){
                    //String attribute = ((ASTAttributeAccess) expr).getAttributeName();
                    String t = env.getExprType(expr, _class);
                    if(t == null){
                        t = "null";
                    }
                    if(t.equals("this")) { t = _class.getName(); }
                    searchInParent(t,actual_pars, methodCalled, r);
                }
                if(expr instanceof ASTLiteral){
                    String varName = ((ASTLiteral) expr).getValue();
                    literalAccess(methodCalled, actual_pars, pkg2, r, varName, env);
                }
                if(expr instanceof ASTMethodCall){
                    Pair<String,String> type = ResolveTypes.getTypeMethodCall(imports, _class, (ASTMethodCall) expr, env);
                    methodAccess(methodCalled, actual_pars, type, r);
                }
                if(expr instanceof ASTCast){
                    String typeName = ((ASTCast) expr).getType();
                    String pkg = ResolveTypes.getImportPkgFromType(imports, typeName);
                    Pair<String,String> type = new Pair<>(typeName, pkg);
                    methodAccess(methodCalled, actual_pars, type, r);
                }
            }
        });
    }

    private void methodAccess(String methodCalled, List<Pair<String, String>> actual_pars, Pair<String, String> type, ASTRE r) {

    }

    private void literalAccess(String methodCalled, List<Pair<String, String>> actual_pars, String pkg2, ASTRE r, String varName, Env env) {
        if(varName.endsWith("this")){ //if is this. check locally
            localCall(methodCalled, actual_pars, pkg2, r);
        } else if(varName.equals("super")) {
            //search in parent class
        } else { //search from the env
            IASTVar var = env.getVar(varName);
            if(var != null){ //we have smth in the env (should be always the case)
                String varType = var.getTypeNoArray();
                if(methods.containsKey(varType)){
                    //exists in the list
                    List<IndexMethod> ms = methods.get(varType);
                    if(containsMethod(methodCalled, actual_pars, ms)){
                        //also the call is in the list -> we gotta a match
                        IndexMethod m = getMethod(methodCalled, actual_pars, ms);
                        if(m != null) {
                            System.out.println("@" + r.getLine() + " -- " + m.getPackageName() + "." + m.getFromClass() + "::" + methodCalled);
                            methodCalls.add(new Triplet<>(
                                    m.getPackageName() + "." + m.getFromClass(),
                                    methodCalled,
                                    r
                            ));
                        }
                    }
                }
            } else {
                //static call
                IndexData c = getFromImport(varName);
                if(c != null){
                    if(containsMethod(methodCalled, actual_pars, c.getListOfSyncMethods())){
                        //also the call is in the list -> we gotta a match
                        IndexMethod m = getMethod(methodCalled, actual_pars, c.getListOfMethods());
                        if(m != null) {
                            System.out.println("@" + r.getLine() + " -- " + m.getPackageName() + "." + m.getFromClass() + "::" + methodCalled);
                            methodCalls.add(new Triplet<>(
                                    m.getPackageName() + "." + m.getFromClass(),
                                    methodCalled,
                                    r
                            ));
                        }
                    }
                }
            }
        }
    }

    private void searchInParent(String t, List<Pair<String, String>> actual_pars, String methodCalled, ASTRE r) {

    }

    private void localCall(String methodCalled, List<Pair<String, String>> actual_pars, String pkg2, ASTRE r) {
        for(IASTMethod m : _class.getAllMethods()){
            if(m.equalsBySignature(pkg2, methodCalled, actual_pars)){
                //System.out.println("@" + r.getLine() + " -- " + c.getPackageName().replace(".","_") + "_" + c.getName() + "::" + methodCalled);
                methodCalls.add(new Triplet<>(
                        this._class.getPackageName() + "." + this._class.getName(),
                        methodCalled,
                        r
                ));
            }
        }
    }
}

