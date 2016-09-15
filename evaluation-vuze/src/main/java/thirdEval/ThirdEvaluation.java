package thirdEval;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import secondEvalPCFG.EvalTopFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ThirdEvaluation {

	final String _NAME_ = "vuze_third";
	boolean isIndexed = false;
	String base_path = "";

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

	private void run() {
		if(isIndexed) indexProject(base_path);
	}

	public void indexProject(String path){
		MongoOptions.getInstance().setDbName(_NAME_);
		IndexingProject indexing = new IndexingProject(_NAME_);
		indexing.delete();
		System.out.println("Finish delete");
		indexing.indexProject(path, false);
		System.out.println("Indexing done");
		indexing.indexSyncBlock(path, false);
		System.out.println("Indexing Blocks done");
		indexing.indexSyncCall(path, false);
		System.out.println("Indexing Calls done");
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
