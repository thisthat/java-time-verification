package secondEvalPCFG;

import intermediateModelHelper.indexing.IndexingProject;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import intermediateModelHelper.indexing.structure.IndexData;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 *
 *
 *
 */
public class FindBugs {

	MongoConnector db;
	PrintWriter[] writer = new PrintWriter[5];
	String[] packages = new String[5];
	private static final String _EVALUATION_NAME = "vuze_top_file";
	private static final int _MAX = 5;

	//parameters
	private boolean _INDEXING = false;
	private String mongoIP = "127.0.0.1";
	private int mongoPORT = 27017;
	private int nThreads = 1;
	private String base_path = "";

	public void set_INDEXING(boolean _INDEXING) {
		this._INDEXING = _INDEXING;
	}

	public void setMongoIP(String mongoIP) {
		this.mongoIP = mongoIP;
	}

	public void setMongoPORT(int mongoPORT) {
		this.mongoPORT = mongoPORT;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	public void setBase_path(String base_path) {
		this.base_path = base_path;
	}

	public static void main(String[] args) throws Exception {
		FindBugs eval = new FindBugs();
		int i = 0;
		while(i < args.length){
			String current = args[i++];
			switch (current){
				case "-t":
					int nThreads = Integer.parseInt(args[i++]);
					eval.setnThreads(nThreads);
					break;
				case "-ip":
					eval.setMongoIP(args[i++]);
					break;
				case "-port":
					eval.setMongoPORT(Integer.parseInt(args[i++]));
					break;
				case "-index":
					eval.set_INDEXING(true);
					break;
				case "-path":
					eval.setBase_path(args[i++]);
					break;
			}
		}
		eval.run();
	}

	private void run() throws Exception {
		conf();
		for(int i = 2; i < _MAX; i++){
			System.out.println("Package " + i + " started");
			process_package(packages[i], i);
		}
		close();
	}

	private void process_package(String aPackage, int i) {
		//List<ASTClass> working_set = computeClasses(aPackage);
		List<ASTClass> working_set = computeFile(aPackage);
		for(ASTClass work_item : working_set){
			List<ASTClass> listOfAllClasses = new ArrayList<>();
			List<IndexData> d = db.getClassesThatImports(work_item.getPackageName(), work_item.getName());
			for(IndexData data : d){
				listOfAllClasses.addAll( computeFile(data.getPath()) );
			}
			//add myself as well!
			listOfAllClasses.addAll(working_set);
			int total = 0;
			for(ASTClass _class : listOfAllClasses){
				for(IASTMethod method_work_item : work_item.getMethods()){
					for(IASTMethod method_class : _class.getMethods()){
						total++;
					}
				}
			}
			System.out.println("[" + work_item.toString() + "] Total number of methods: " + total);
			int current = 0;
			for(ASTClass _class : listOfAllClasses){
				for(IASTMethod method_work_item : work_item.getMethods()){
					for(IASTMethod method_class : _class.getMethods()){
						current++;
						double perc = Math.floor(((double)current / (double)total * 100) * 1000) / 1000;
						if(perc < 0.48f) continue;
						System.out.print(String.format("\r[%s %%]", perc ));
						compare(work_item, method_work_item, _class, method_class, i);
					}
				}
			}
			System.out.println();
		}
	}

	private void compare(ASTClass work_item, IASTMethod method_work_item, ASTClass aClass, IASTMethod method_class, int i) {
		IM2PCFG p = new IM2PCFG();
		p.addClass(work_item, method_work_item);
		p.addClass(aClass , method_class);
		PCFG graph = p.buildPCFG();
		int timeConstraint = p.getConstraintsSize();
		int numberSync = graph.getESync().size();
		String pkgOut = work_item.getPackageName();
		String pkgIn = aClass.getPackageName();
		//System.out.println(cOut.getName() + ":" + mOut.getName() + "_vs_" + cIn.getName() + ":" + mIn.getName() + " = " + timeConstraint + "," + numberSync);
		writer[i].println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;", pkgOut, work_item.getName(), method_work_item.getName(), pkgIn, aClass.getName(), method_class.getName(), timeConstraint, numberSync, (timeConstraint + numberSync) ));
		writer[i].flush();
	}

	private void conf() throws Exception {
		//init db
		MongoOptions.getInstance().setDbName(_EVALUATION_NAME);
		MongoOptions.getInstance().setIp(mongoIP);
		this.db = MongoConnector.getInstance();
		if(_INDEXING) this.db.drop();
		//init output file
		for(int i = 0; i < _MAX; i++) {
			writer[i] = new PrintWriter("result_" + i + "_" + this.getClass().getSimpleName().toString() + ".csv", "UTF-8");
			writer[i].println("package-out;class-name-out;method-name-out;package-in;class-name-in;method-name-in;time-constraint;number-sync;total;");
		}
		//calculate directories
		packages[0] = base_path + "com/aelitis/azureus/plugins/net/buddy/BuddyPluginBuddy.java"; //the one with highest number of sync block that is imported at least once
		packages[1] = base_path + "org/gudy/azureus2/core3/util/ThreadPool.java"; //the class with highest number of import and above avg number of sync block. The avg is 2 (max 4), if we restrict to elms with only sync block the avg is 3 (max 6)
		packages[2] = base_path + "org/gudy/azureus2/core3/util/TorrentUtils.java"; //as before but here the number of sync blocks is 10
		packages[3] = base_path + "org/gudy/azureus2/core3/util/Timer.java"; //class with the highest number of sync methods that is included in the most number of files
		packages[4] = base_path + "org/gudy/azureus2/pluginsimpl/local/PluginInitializer.java"; //promising class. high number of syncblock and avg imported

		//index whole vuze
		if(_INDEXING) new IndexingProject(_EVALUATION_NAME).indexProject(base_path, true);
	}

	private void close() {
		for(int i = 0; i < _MAX; i++) {
			writer[i].close();
		}
	}

	private List<ASTClass> computeClasses(String _dir) {
		File dir = new File(_dir);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		List<ASTClass> out = new ArrayList<>();
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			out.addAll(computeFile(filename));
		}
		return out;
	}
	private List<ASTClass> computeFile(String filename){
		Java2AST a = null;
		List<ASTClass> out = new ArrayList<>();
		try {
			a = new Java2AST(filename, true);
		} catch (IOException e) {
			e.printStackTrace();
			return out;
		}
		CompilationUnit result = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(result, filename);
		result.accept(v);
		out.addAll(v.listOfClasses);
		return out;
	}
}
