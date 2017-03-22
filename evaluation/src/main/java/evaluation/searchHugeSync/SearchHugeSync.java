package evaluation.searchHugeSync;

import intermediateModelHelper.indexing.IndexingProject;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import intermediateModelHelper.indexing.structure.IndexSyncCall;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import PCFG.structure.edge.SyncEdge;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.javatuples.Pair;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SearchHugeSync {

	public class GeneralInfo {
		public long syncBlock  = 0;
		public long syncMustbe = 0;
		public long syncMaybe  = 0;

		public long getTotal(){
			return syncBlock + syncMaybe + syncMustbe;
		}

		@Override
		public String toString() {
			String out = "";
			out += "syncBlock: " + syncBlock + "\n";
			out += "syncMust:  " + syncMustbe + "\n";
			out += "syncMay:   " + syncMaybe + "\n";
			return  out;
		}
	}

	boolean isIndexed = false;
	String base_path = "/Users/giovanni/repository/java-xal/project_eval/";
	String _NAMES_[] = {"activemq","airavata","jetty","vuze","wildfly-core"};
	String _NAME_ = _NAMES_[0];
	Map<Pair<String,String>,GeneralInfo> statistics = new HashMap<>();

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		SearchHugeSync eval = new SearchHugeSync();
		int i = 0;
		while(i < args.length){
			String current = args[i++];
			switch (current){
				case "-index":
					eval.isIndexed = true;
					break;
				case "-path":
					eval.base_path = args[i++];
					break;
				case "-name":
					eval._NAME_ = args[i++];
					break;
			}
		}
		//for(String name : eval._NAMES_){
			//eval._NAME_ = name;
			eval.run();
		//}
	}

	double start_run = 0;

	private void run() throws FileNotFoundException, UnsupportedEncodingException {
		start_run = new Date().getTime();
		System.out.println("\n[Working with: " + _NAME_ + "]");
		System.out.println("Options:");
		if(this.isIndexed) System.out.println("\t[indexing]");
		System.out.println("\t[path: " + this.base_path + this._NAME_ + "]\n");
		conf();
		if(isIndexed) indexProject(this.base_path + this._NAME_ );
		eval();
		//System.out.println(statistics);
	}

	private void conf() throws FileNotFoundException, UnsupportedEncodingException {
		MongoOptions.getInstance().setDbName(_NAME_);
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
		System.out.println("[Indexing] "+ (end-start)/1000 + " s");
	}

	private void eval() throws FileNotFoundException, UnsupportedEncodingException {
		double start = new Date().getTime();
		Datastore mongo = MongoConnector.getInstance().getDatastore();

		Query<IndexSyncCall> qBlock = mongo.createQuery(IndexSyncCall.class);
		List<IndexSyncCall> synccall = qBlock.asList();
		List<Pair<String,String>> classesVisited = new ArrayList<>();
		for(int i = 0; i < synccall.size(); i++){
			for(int j = i + 1; j < synccall.size(); j++){
				IndexSyncCall s1 = synccall.get(i);
				IndexSyncCall s2 = synccall.get(j);
				if(!s1.isStatic() || !s2.isStatic()) continue;
				if(s1 == s2) continue;
				if(!s1.getMethodName().equals(s2.getMethodName()) || !s1.getClassName().equals(s2.getClassName())) continue;

				Pair<String,String> k = new Pair(s1.get_inClassPackage() + "." + s1.get_inClassName(), s2.get_inClassPackage() + "." + s2.get_inClassName() );
				if(classesVisited.contains(k)) continue;
				classesVisited.add(k);

				GeneralInfo info;
				if(statistics.containsKey(k)){
					info = statistics.get(k);
				} else {
					info = new GeneralInfo();
				}
				//compute
				List<ASTClass> first = JDTVisitor.parse(s1.getPath());
				List<ASTClass> second = JDTVisitor.parse(s2.getPath());
				IM2PCFG pcfg = new IM2PCFG();
				pcfg.setForceReindex(false);
				for(ASTClass c : first){
					pcfg.addClass(c);
				}
				for(ASTClass c : second){
					pcfg.addClass(c);
				}
				PCFG g = pcfg.buildPCFG();
				for(SyncEdge e : g.getESync()){
					if(e.getType() == SyncEdge.TYPE.SYNC_BLOCK){
						info.syncBlock++;
					}
					if(e.getType() == SyncEdge.TYPE.MUST_SYNC){
						info.syncMustbe++;
					}
					if(e.getType() == SyncEdge.TYPE.MAY_SYNC){
						info.syncMaybe++;
					}
				}
				//System.out.println(info);
				statistics.put(k, info);
			}
		}


		/*
		Query<IndexSyncBlock> qBlock = mongo.createQuery(IndexSyncBlock.class);
		List<IndexSyncBlock> syncsblock = qBlock.asList();
		List<Pair<String,String>> classesVisited = new ArrayList<>();


		for(IndexSyncBlock s1 : syncsblock){
			for(IndexSyncBlock s2 : syncsblock){
				if(s1 == s2) continue;
				if(!DataTreeType.checkCompatibleTypes( s1.getExprType(), s2.getExprType(), s1.getExprPkg(), s2.getExprPkg() )){
					continue;
				}

				Pair<String,String> k = new Pair(s1.getPackageName() + "." + s1.getName(), s2.getPackageName() + "." + s2.getName() );

				//don't do twice
				if(classesVisited.contains(k)) continue;
				classesVisited.add(k);


				GeneralInfo info;
				if(statistics.containsKey(k)){
					info = statistics.get(k);
				} else {
					info = new GeneralInfo();
				}
				//compute
				List<ASTClass> first = JDTVisitor.parse(s1.getPath());
				List<ASTClass> second = JDTVisitor.parse(s2.getPath());
				IM2PCFG pcfg = new IM2PCFG();
				pcfg.setForceReindex(false);
				for(ASTClass c : first){
					pcfg.addClass(c);
				}
				for(ASTClass c : second){
					pcfg.addClass(c);
				}
				PCFG g = pcfg.buildPCFG();
				for(SyncEdge e : g.getESync()){
					if(e.getType() == SyncEdge.TYPE.SYNC_BLOCK){
						info.syncBlock++;
					}
					if(e.getType() == SyncEdge.TYPE.MUST_SYNC){
						info.syncMustbe++;
					}
					if(e.getType() == SyncEdge.TYPE.MAY_SYNC){
						info.syncMaybe++;
					}
				}
				//System.out.println(info);
				statistics.put(k, info);
			}
		}
		*/

		GeneralInfo _max;
		long maxVal = 0;
		List<Pair<Pair<String,String>,GeneralInfo>> list = new ArrayList<>();
		for(Pair<String,String> k : statistics.keySet()){
			GeneralInfo v = statistics.get(k);
			list.add(new Pair<>(k,v));
		}
		Collections.sort(list, (left, right) -> (int)(right.getValue1().getTotal() - left.getValue1().getTotal()));
		List<Pair<Pair<String,String>,GeneralInfo>> top5 = new ArrayList<>();
		for(int j = 0; j < 5; j++){
			top5.add(list.get(j));
		}
		PrintWriter writerBlocks = new PrintWriter(_NAME_ + "_huge.csv", "UTF-8");
		writerBlocks.println("first;second;total");
		for(int k = 0; k < top5.size(); k++){
			Pair<Pair<String,String>,GeneralInfo> e = top5.get(k);
			String first = e.getValue0().getValue0();
			String second = e.getValue0().getValue1();
			long total = e.getValue1().getTotal();
			writerBlocks.println(String.format("%s;%s;%d", first,second,total));
			writerBlocks.flush();
		}
		writerBlocks.close();
		double end = new Date().getTime();
		System.out.println("\n[Eval] "+(end-start)/1000 + " s");
		//send_email( _NAME_ + " finished :: " + (end - start_run)/1000 + "s");
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

}

