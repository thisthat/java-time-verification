package server.handler;

import IntermediateModelHelper.indexing.IndexingProject;
import com.sun.net.httpserver.HttpExchange;
import server.handler.middleware.indexMW;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class OpenProject extends indexMW {

	static Object lock = new Object();

	class IndexingThread extends Thread {
		String name;
		String base_path;
		boolean delete;
		HashMap<String, Boolean> indexProcess;

		public IndexingThread(String name, String base_path, boolean delete, HashMap<String, Boolean> indexProcess) {
			super();
			this.name = name;
			this.base_path = base_path;
			this.delete = delete;
			this.indexProcess = indexProcess;
		}

		@Override
		public void run() {
			boolean flag = true;
			synchronized (lock){
				flag = indexProcess.containsKey(name) ? indexProcess.get(name) : true;
				indexProcess.put(name, true);
			}
			if(flag) {
				IndexingProject index = new IndexingProject(name);
				index.indexProject(base_path, delete);
			}
			synchronized (lock){
				indexProcess.put(name, false);
			}
		}
	}

	String par1 = "path";
	String par2 = "invalidCache";

	HashMap<String, Boolean> indexProcess = new HashMap<>();
	public void handle(HttpExchange he, Map<String, String> parameters, String name) throws IOException {

		//validate input
		boolean flag = true;
		if(!parameters.containsKey(par1)){
			flag = false;
		}
		if(!flag){
			String response = "Incorrect format for the parameters.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		String base_path = parameters.get(par1);
		boolean delete = parameters.containsKey(par2) && parameters.get(par2).equals("1");
		if(!base_path.startsWith("file://")){
			String response = "Unsupported protocol. atm only file:// is supported.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		base_path = base_path.replace("file://","");

		if(!indexProcess.containsKey(name) || !indexProcess.get(name)) {
			IndexingThread thread = new IndexingThread(name, base_path, delete, indexProcess);
			thread.start();
			//compute
			String response = "{\n\tstatus: \"1\"\n}\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		} else {
			String response = "{\n\tstatus: \"0\"\n}\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}


}
