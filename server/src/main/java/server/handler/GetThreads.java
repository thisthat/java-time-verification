package server.handler;

import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import server.handler.middleware.indexMW;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GetThreads extends indexMW {


	class OutputData {
		String path;
		String className;
		String packageName;

		public OutputData(String path, String className, String packageName) {
			this.path = path;
			this.className = className;
			this.packageName = packageName;
		}

		public OutputData(IndexData d) {
			this.path = d.getPath();
			this.className = d.getClassName();
			this.packageName = d.getFullclassPackage();
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}
	}

	@Override
	protected void handle(HttpExchange he, Map<String, String> parameters, String name) throws IOException {
		MongoConnector db = MongoConnector.getInstance(name);
		if(!db.getIndexStatus()){
			String response = "Indexes are not yet computed.";
			he.sendResponseHeaders(406, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		List<IndexData> threads = db.getThreads();
		List<OutputData> files = new ArrayList<>();
		for(IndexData d : threads){
			if(! files.contains(d.getPath()) )
				files.add(new OutputData(d));
		}
		ObjectMapper json = new ObjectMapper();
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(files);
		he.getResponseHeaders().add("Content-Type","application/json");
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();

	}
}
