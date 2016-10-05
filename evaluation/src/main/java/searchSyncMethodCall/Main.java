package searchSyncMethodCall;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	private static final String _BROCK_IP_ = "143.205.114.27";

	public class GeneralInfo {
		public long syncCalls = 0;
		public long nClasses = 0;
		public long nMethods = 0;


		public long methodSyncCall = 0;
		public long nSyncMethods = 0;
		public long nMaybe = 0;

		@Override
		public String toString() {
			String out = "";
			out += String.format("#Classes: %d \n", this.nClasses);
			out += String.format("#Methods: %d \n", this.nMethods);
			out += String.format("#Synchronized Methods: %d \n", this.nSyncMethods);
			out += String.format("#Methods Sync Called: %d \n", this.methodSyncCall);
			out += String.format("#Maybe Call: %d \n", this.nMaybe);
			out += String.format("Total #Sync Calls: %d \n", this.syncCalls);
			return  out;
		}
	}
	String _NAMES_[] = {"activemq","airavata","jetty","vuze","wildfly-core"};
	String base_path = "/Users/giovanni/repository/java-xal/project_eval/";
	GeneralInfo statistics = new GeneralInfo();

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Main eval = new Main();
		eval.run();
	}

	public void indexProject(String path, String name){
		double start = new Date().getTime();
		IndexingProject indexing = new IndexingProject(name);
		indexing.delete();
		System.out.println("Finish delete");
		int i = indexing.indexProject(path, false);
		System.out.println("Indexing done: " + i);
		//indexing.indexSyncBlock(path, false);
		//System.out.println("Indexing Blocks done");
		deleteCollection(name);
		indexing.indexSyncCall(path, false);
		System.out.println("Indexing Calls done");
		double end = new Date().getTime();
		System.out.println("[Indexing] "+ (end-start)/1000 + " s");
	}

	private void deleteCollection(String name){
		MongoDatabase mongo = MongoConnector.getInstance(name).getDb();
		mongo.getCollection("IndexSyncCall").drop();
	}

	private void run() throws Exception {
		for(int i = 0; i < _NAMES_.length; i++){
			String name = _NAMES_[i];
			conf(name);
			indexProject(base_path + name, name);
			eval(name);
			System.out.println("\n[Working with: " + name + "]");
			System.out.println(statistics);
		}

	}

	private void conf(String name) throws Exception {
		MongoOptions options = MongoOptions.getInstance();
		options.setDbName(name);
		statistics = new GeneralInfo();
		//options.setIp(_BROCK_IP_);
	}

	private void eval(String name) throws Exception {

		MongoDatabase db = MongoConnector.getInstance(name).getDb();
		Datastore mongo = MongoConnector.getInstance(name).getDatastore();

		MongoCollection<Document> d;
		d = db.getCollection("IndexSyncCall");
		statistics.syncCalls = d.count();
		d = db.getCollection("IndexData");
		statistics.nClasses = d.count();

		List<Quartet<String, String, String,List<String>>> syncMethodNames = new ArrayList<>();

		Query<IndexData> all = mongo.createQuery(IndexData.class);
		int nMethods = 0;
		int nSyncMethod = 0;
		for(IndexData data : all.asList()){
			nMethods += data.getListOfMethods().size();
			nSyncMethod += data.getListOfSyncMethods().size();
			for(IndexMethod m : data.getListOfSyncMethods()){
				List<String> pars = new ArrayList<>();
				for(IndexParameter p : m.getParameters()){
					pars.add(p.getType());
				}
				Quartet<String, String, String,List<String>> entry = new Quartet<>(m.getPackageName(), m.getFromClass(), m.getName(), pars);
				syncMethodNames.add(entry);
			}
		}
		statistics.nMethods = nMethods;
		statistics.nSyncMethods = nSyncMethod;

		Query<IndexSyncCall> qCall = mongo.createQuery(IndexSyncCall.class);
		List<IndexSyncCall> syncsCall = qCall.asList();
		List<Quartet<String, String, String,List<String>>> methodsCall = new ArrayList<>();
		List<Quartet<String, String, String,List<String>>> methodsWhereSyncCall = new ArrayList<>();
		for(IndexSyncCall s : syncsCall){
			Quartet<String,String,String,List<String>> m = new Quartet<>(s.get_inClassPackage(), s.get_inClassName(), s.getInMethodName(),s.getSignatureInMethod());
			if(!methodsWhereSyncCall.contains(m)){
				methodsWhereSyncCall.add(m);
			}
			Quartet<String,String,String,List<String>> m1 = new Quartet<>(s.getPackageName(), s.getClassName(), s.getMethodName(),s.getMethodSignature());
			if(!methodsCall.contains(m1)){
				methodsCall.add(m1);
			}
			if(s.getType() == SyncMethodCall._SYNC_CALL_MAYBE_){
				statistics.nMaybe++;
			}
		}

		PrintWriter write;
		/*
		write = new PrintWriter(name + "_listWhereSyncMethodAreCalled.csv");
		for(Quartet<String, String, String,List<String>> entry : methodsWhereSyncCall){
			write.println(ppCSV(entry));
		}
		write.close();
		*/
		statistics.methodSyncCall = 0;
		write = new PrintWriter(name + "_listSyncMethod.csv");
		for(Quartet<String, String, String,List<String>> entry : syncMethodNames){
			boolean isCalled = false;
			for(Quartet<String, String, String,List<String>> e : methodsCall){
				if(		e.getValue0().equals(entry.getValue0()) &&
						e.getValue1().equals(entry.getValue1())	&&
						e.getValue2().equals(entry.getValue2())
					){
					isCalled = true;
				}
			}
			if(isCalled) statistics.methodSyncCall++;
			write.println(pp(entry) + ";" + isCalled);
		}
		write.close();

		//search for methods -> not praticable
		//search(name, syncMethodNames);

	}

	private void search(String name, List<Quartet<String, String, String, List<String>>> syncMethodNames) throws Exception {
		File dir = new File(base_path + name);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();

		List<Pair<String,String>> whereItIs = new ArrayList<>();
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(filename.contains("_mvn")) continue; //skip the mvn version of vuze
			FileInputStream inputStream = new FileInputStream(filename);
			String filecnt = IOUtils.toString(inputStream);
			for(Quartet<String, String, String, List<String>> m : syncMethodNames){
				if(filecnt.contains(m.getValue2() + "(")){
					whereItIs.add(new Pair<>(filename, m.getValue2()));
				}
			}
		}
		PrintWriter write = new PrintWriter(name + "_listSyncMethodCalled.csv");
		for(Pair<String,String> entry : whereItIs){
			write.println(entry.getValue0() + ";" + entry.getValue1());
		}
		write.close();
	}

	private String pp(Quartet<String, String, String,List<String>> entry){
		String out = entry.getValue0() + "." + entry.getValue1() + "." + entry.getValue2() + "(";
		for(int i = 0; i < entry.getValue3().size(); i++){
			out += entry.getValue3().get(i) + (entry.getValue3().size() == (i+1) ? "" : ",");
		}
		out += ")";
		return out;
	}

	private String ppCSV(Quartet<String, String, String,List<String>> entry){
		String out = entry.getValue0() + ";" + entry.getValue1() + ";" + entry.getValue2() + ";[";
		for(int i = 0; i < entry.getValue3().size(); i++){
			out += entry.getValue3().get(i) + (entry.getValue3().size() == (i+1) ? "" : ",");
		}
		out += "]";
		return out;
	}

}

