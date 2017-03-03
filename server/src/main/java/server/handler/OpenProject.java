package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import intermediateModelHelper.indexing.IndexingProject;
import com.sun.net.httpserver.HttpExchange;
import server.handler.middleware.ParsePars;
import server.handler.middleware.indexMW;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
			boolean flag;
			synchronized (lock){
				flag = indexProcess.containsKey(name) ? indexProcess.get(name) : true;
				indexProcess.put(name, true);
			}
			if(flag) {
				IndexingProject index = new IndexingProject(name);
				index.setSkipTest(false);
				index.indexProject(base_path, delete);
			}
			synchronized (lock){
				indexProcess.put(name, false);
			}
		}
	}

	public HashMap<String, Boolean> getIndexProcess() {
		return indexProcess;
	}

	class Status {
		int status;
		String description;

		public Status(int status, String description) {
			this.status = status;
			this.description = description;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
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
			ParsePars.printErrorMessagePars(he);
			return;
		}
		String base_path = parameters.get(par1);
		boolean delete = parameters.containsKey(par2) && parameters.get(par2).equals("1");
		if(!ParsePars.parseFileUrl(base_path, he)){
			return;
		}
		base_path = base_path.replace("file://","");

		Status msg;
		//does the path exists?
		if (new File(base_path).exists()) {
			if(!indexProcess.containsKey(name) || !indexProcess.get(name)) {
				IndexingThread thread = new IndexingThread(name, base_path, delete, indexProcess);
				thread.start();
				//compute
				msg = new Status(0, "");
			} else {
				msg = new Status(1, "A process is already parsing this folder");
			}
		} else {
			msg = new Status(1, "Path does not exists");
		}

		ObjectMapper json = ParsePars.getOutputFormat(parameters);
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(msg);
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}


}
