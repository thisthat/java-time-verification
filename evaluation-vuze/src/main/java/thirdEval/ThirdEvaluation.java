package thirdEval;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import IntermediateModelHelper.indexing.structure.IndexSyncCall;
import IntermediateModelHelper.types.DataTreeType;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import PCFG.structure.edge.SyncEdge;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.bson.Document;
import org.javatuples.Quartet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ThirdEvaluation {

	public class GeneralInfo {
		public long syncBlock = 0;
		public long syncCalls = 0;
		public long nClasses = 0;
		public long nMethods = 0;

		public long methodSyncBlock = 0;
		public long methodSyncCall = 0;
		public long methodBoth = 0;

		public long methodTimeBlock = 0;
		public long methodTimeCalls = 0;
		public long methodTimeBoth = 0;

		@Override
		public String toString() {
			String out = "";
			out += String.format("#Classes: %d \n", this.nClasses);
			out += String.format("#Methods: %d \n", this.nMethods);
			out += String.format("#Sync Blocks: %d \n", this.syncBlock);
			out += String.format("#Sync Calls: %d \n", this.syncCalls);

			out += String.format("#Methods w/ Sync Blocks: %d \n", this.methodSyncBlock);
			out += String.format("#Methods w/ Sync Calls: %d \n", this.methodSyncCall);
			out += String.format("#Methods w/ Both: %d \n", this.methodBoth);

			out += String.format("#Methods w/ Time + Sync Blocks: %d \n", this.methodTimeBlock);
			out += String.format("#Methods w/ Time + Sync Calls: %d \n", this.methodTimeCalls);
			out += String.format("#Methods w/ Time + Both: %d \n", this.methodTimeBoth);

			return  out;
		}
	}

	final String _NAME_ = "vuze_third";
	boolean isIndexed = false;
	String base_path = "";
	GeneralInfo statistics = new GeneralInfo();
	PrintWriter writerBlocks;
	PrintWriter writerCalls;

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		ThirdEvaluation eval = new ThirdEvaluation();
		int i = 0;
		while(i < args.length){
			String current = args[i++];
			switch (current){
				case "-index":
					eval.setIndexed(true);
					break;
				case "-path":
					eval.setBase_path(args[i++]);
					break;
			}
		}
		eval.run();
	}

	enum EvalType {
		BLOCKS,
		CALLS,
		BOTH
	}

	private void run() throws FileNotFoundException, UnsupportedEncodingException {
		conf();
		if(isIndexed) indexProject(base_path);
		eval();
		System.out.println(statistics);
	}

	private void conf() throws FileNotFoundException, UnsupportedEncodingException {
		MongoOptions.getInstance().setDbName(_NAME_);
		writerBlocks = new PrintWriter("syncBlocks.csv", "UTF-8");
		writerBlocks.println("package-out;class-name-out;method-name-out;package-in;class-name-in;method-name-in;time-constraint;number-sync;total");
		writerCalls = new PrintWriter("syncBlocks.csv", "UTF-8");
		writerCalls.println("package-out;class-name-out;method-name-out;package-in;class-name-in;method-name-in;time-constraint;number-sync;total");
	}

	public void indexProject(String path){
		double start = new Date().getTime();
		IndexingProject indexing = new IndexingProject(_NAME_);
		indexing.delete();
		System.out.println("Finish delete");
		indexing.indexProject(path, false);
		System.out.println("Indexing done");
		indexing.indexSyncBlock(path, false);
		System.out.println("Indexing Blocks done");
		indexing.indexSyncCall(path, false);
		System.out.println("Indexing Calls done");
		double end = new Date().getTime();
		System.out.println("[Indexing] "+(end-start)/1000 + " s");
	}

	private void eval() {
		double start = new Date().getTime();
		MongoDatabase db = MongoConnector.getInstance().getDb();
		Datastore mongo = MongoConnector.getInstance().getDatastore();

		MongoCollection<Document> d;
		d = db.getCollection("IndexSyncBlock");
		statistics.syncBlock = d.count();
		d = db.getCollection("IndexSyncCall");
		statistics.syncCalls = d.count();
		d = db.getCollection("IndexData");
		statistics.nClasses = d.count();

		Query<IndexData> all = mongo.createQuery(IndexData.class);
		int nMethods = 0;
		for(IndexData data : all.asList()){
			nMethods += data.getListOfMethods().size();
		}
		statistics.nMethods = nMethods;

		Query<IndexSyncBlock> qBlock = mongo.createQuery(IndexSyncBlock.class);
		List<IndexSyncBlock> syncsblock = qBlock.asList();
		List<Quartet<String, String, String,List<String>>> methodsBlock = new ArrayList<>();
		for(IndexSyncBlock s : syncsblock){
			Quartet<String,String,String,List<String>> m = new Quartet<>(s.getPackageName(), s.getName(), s.getMethodName(),s.getSignature());
			if(!methodsBlock.contains(m)){
				methodsBlock.add(m);
			}
		}
		statistics.methodSyncBlock = methodsBlock.size();

		Query<IndexSyncCall> qCall = mongo.createQuery(IndexSyncCall.class);
		List<IndexSyncCall> syncsCall = qCall.asList();
		List<Quartet<String, String, String,List<String>>> methodsCall = new ArrayList<>();
		for(IndexSyncCall s : syncsCall){
			Quartet<String,String,String,List<String>> m = new Quartet<>(s.get_inClassPackage(), s.get_inClassName(), s.getInMethodName(),s.getSignatureInMethod());
			if(!methodsCall.contains(m)){
				methodsCall.add(m);
			}
		}
		statistics.methodSyncCall = methodsCall.size();

		long methodBoth = 0;
		for(Quartet<String,String,String,List<String>> m1 : methodsBlock){
			for(Quartet<String,String,String,List<String>> m2 : methodsCall){
				if(m1.equals(m2)){
					methodBoth++;
				}
			}
		}
		statistics.methodBoth = methodBoth;

		evalSyncBlocks(syncsblock);

		double end = new Date().getTime();
		System.out.println("[Eval] "+(end-start)/1000 + " s");
	}

	private void evalSyncBlocks(List<IndexSyncBlock> methodsBlock) {
		int total = methodsBlock.size() * methodsBlock.size();
		int current = 0;
		for(IndexSyncBlock first : methodsBlock){
			for(IndexSyncBlock second : methodsBlock){
				current++;
				double perc = Math.floor(((double)current / (double)total * 100) * 1000) / 1000;
				System.out.print(String.format("\r[%s %%]", perc ));
				if(DataTreeType.checkCompatibleTypes( first.getExprType(), second.getExprType(), first.getExprPkg(), second.getExprPkg() )){
					compare(first, second);
				}
			}
		}
	}

	public void compare(IndexSyncBlock m1, IndexSyncBlock m2){
		List<ASTClass> classes_m1  = JDTVisitor.parse(m1.getPath());
		List<ASTClass> classes_m2  = JDTVisitor.parse(m2.getPath());
		ASTClass class_1 = null, class_2 = null;
		IASTMethod method_1 = null, method_2 = null;
		for(ASTClass c : classes_m1){
			if(c.getName().equals(m1.getName())){
				class_1 = c;
			}
		}
		for(ASTClass c : classes_m2){
			if(c.getName().equals(m2.getName())){
				class_2 = c;
			}
		}
		String methodName = m1.getMethodName();
		List<String> signature = m1.getSignature();
		for(IASTMethod m : class_1.getMethods()){
			if(m.getName().equals(methodName)) {
				if(m.getParameters().size() == signature.size()){
					boolean flag = true;
					for(int i = 0; i < signature.size(); i++){
						if(!m.getParameters().get(i).getType().equals(signature.get(i))){
							flag = false;
						}
					}
					if(flag){
						method_1 = m;
					}
				}
			}
		}
		methodName = m2.getMethodName();
		signature = m2.getSignature();
		for(IASTMethod m : class_2.getMethods()){
			if(m.getName().equals(methodName)) {
				if(m.getParameters().size() == signature.size()){
					boolean flag = true;
					for(int i = 0; i < m.getParameters().size(); i++){
						if(!m.getParameters().get(i).getType().equals(signature.get(i))){
							flag = false;
						}
					}
					if(flag){
						method_2 = m;
					}
				}
			}
		}
		if(class_1 == null || method_1 == null || class_2 == null || method_2 == null){
			System.err.println("Null");
			return;
		}
		compare(class_1, method_1, class_2, method_2, EvalType.BLOCKS);
	}

	private void compare(ASTClass class_1, IASTMethod method_1, ASTClass class_2, IASTMethod method_2, EvalType type) {
		IM2PCFG p = new IM2PCFG();
		p.addClass(class_1, method_1, false);
		p.addClass(class_2, method_2, false);
		PCFG graph = p.buildPCFG();
		int timeConstraint = p.getConstraintsSize();
		int numberSyncBlock = 0;
		int numberSyncCall = 0;
		for(SyncEdge e : graph.getESync()){
			if(e.getType() == SyncEdge.TYPE.SYNC_BLOCK){
				numberSyncBlock++;
			} else {
				numberSyncCall++;
			}
		}
		String pkgOut = class_1.getPackageName();
		String cOut = class_1.getName();
		String mOut = method_1.getName();
		String pkgIn = class_2.getPackageName();
		String cIn = class_2.getName();
		String mIn = method_2.getName();

		if(type == EvalType.BLOCKS) {
			String out = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s", pkgOut, cOut, mOut, pkgIn, cIn, mIn, timeConstraint, numberSyncBlock, (timeConstraint + numberSyncBlock));
			writerBlocks.println(out);
			writerBlocks.flush();
		} else {
			String out = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s", pkgOut, cOut, mOut, pkgIn, cIn, mIn, timeConstraint, numberSyncCall, (timeConstraint + numberSyncCall));
			writerCalls.println(out);
			writerCalls.flush();
		}
	}


	private void send_email(String msg) {
		try {
			msg = URLEncoder.encode(msg, "UTF-8");
			String a= "http://sensarisvolti.altervista.org/send_email.php?msg=" + msg;
			URL url = new URL(a);
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			br.read();
			br.close();
			br = null;
			conn = null;
			url = null;
		} catch (Exception e){
			//is not a problem if we can not send the email
		}
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public void setIndexed(boolean indexed) {
		isIndexed = indexed;
	}

	public void setBase_path(String base_path) {
		this.base_path = base_path;
	}

}

