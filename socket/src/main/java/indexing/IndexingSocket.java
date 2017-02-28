package indexing;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTAttribute;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTCast;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.structure.expression.ASTVariableDeclaration;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.EnvBase;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.structure.IndexSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 27/02/2017.
 */
public class IndexingSocket extends ParseIM {
    MongoConnector mongo;
    ASTClass _c;
    List<IndexSocket> output = new ArrayList<>();
    List<String> socketTypes = new ArrayList<>();
    static List<String> socketBaseTypes = new ArrayList<>();
    String lastMethodName = "";

    static {
        socketBaseTypes.add("Socket");
        socketBaseTypes.add("ServerSocket");
        socketBaseTypes.add("SocketInputStream");
        socketBaseTypes.add("DatagramSocket");
        socketBaseTypes.add("MulticastSocket");
        socketBaseTypes.add("SSLSocket");
        socketBaseTypes.add("SSLServerSocket");
    }

    public IndexingSocket(MongoConnector mongo) {
        this(mongo, new ArrayList<>());
    }
    public IndexingSocket(MongoConnector mongo, List<String> extendedSocketTypes) {
        this.mongo = mongo;
        socketTypes.addAll(socketBaseTypes);
        socketTypes.addAll(extendedSocketTypes);
    }

    /**
     * Start the indexing the calls of synchronized method of a {@link ASTClass}.
     * It force to delete the index structure from the DB and recreate it.
     *
     * @param c	Class to analyse.
     * @return	The list of indexes of sync calls of the class.
     */
    public void index(ASTClass c){
        index(c, false);
    }

    /**
     * Start the indexing the calls of synchronized method of a {@link ASTClass}.
     * It force to delete the index structure from the DB and to recreate it if the <i>forceReindex</i> flag is set to true.
     * @param c	Class to analyze
     * @param forceReindex flag to force the recreation of the index
     * @return	The list of indexes of sync calls of the class.
     */
    public void index(ASTClass c, boolean forceReindex) {
        this._c = c;
        super.set_class(c);
        if(mongo.existIndexSocketClass(c)){
            if(forceReindex){
                mongo.deleteSyncBlock(c);
            } else {
                return;
            }
        }
        createBaseEnv(c);
    }

    /**
     * The following method creates the basic environment for a class.
     * @param c Class to analyze
     */
    @Override
    protected EnvBase createBaseEnv(ASTClass c){
        Env base = super.createBaseEnv(c);
        //check in the attributes
        for(ASTAttribute a : c.getAttributes()){
            analyzeAttribute(a, base);
        }
        for(IASTMethod m : c.getMethods()){
            analyzeMethod(m, base);
        }
        return base_env;
    }

    public static List<String> getSocketBaseTypes() {
        return socketBaseTypes;
    }

    public List<String> getSocketTypes() {
        return socketTypes;
    }

    @Override
    protected void analyzeMethod(IASTMethod method, Env e) {
        lastMethodName = method.getName();
        for(IASTVar v : e.getVarList()){
            checkSocketType(v.getType(), method.getLine());
        }
        super.analyzeMethod(method, e);
    }

    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        if(r != null && r.getExpression() != null){
            r.getExpression().visit(new DefualtASTREVisitor(){
                @Override
                public void enterASTVariableDeclaration(ASTVariableDeclaration elm) {
                    checkSocketType(elm.getType(), elm.getLine());
                }

                @Override
                public void enterASTNewObject(ASTNewObject elm) {
                    checkSocketType(elm.getTypeName(), elm.getLine());
                }

                @Override
                public void enterASTCast(ASTCast elm) {
                    checkSocketType(elm.getType(), elm.getLine());
                }
            });

        }
    }

    private void analyzeAttribute(ASTAttribute a, Env base) {
        checkSocketType(a.getType(), a.getLine());
    }

    private void checkSocketType(String elm, int line) {
        if(socketTypes.contains(elm)){
            //System.out.println("[FOUND] " + more + " :: " + elm + " @ " + _c.getPath());
            IndexSocket s = new IndexSocket(line, _c.getPath(), _c.getName(), lastMethodName);
            mongo.add(s);
        }
    }
}
