package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import server.handler.middleware.ParsePars;
import server.handler.middleware.indexMW;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class getFile extends indexMW {
	String par1 = "filePath";

	String lastFileServed = "";

	@Override
	protected void handle(HttpExchange he, Map<String, String> parameters, String name) throws IOException {
		//validate input
		boolean flag = true;
		if(!parameters.containsKey(par1)){
			flag = false;
		}
		if(!flag){
			ParsePars.printErrorMessagePars(he);
			return;
		}
		String file_path = parameters.get(par1);
		if(!ParsePars.parseFileUrl(file_path, he)){
			return;
		}
		file_path = file_path.replace("file://","");

		//get base path of project
		MongoConnector mongo = MongoConnector.getInstance(name);
		String base_path = mongo.getBasePath();

		String file = base_path + "/" + file_path;
		//avoid bad path
		file = file.replace("//","/");
		lastFileServed = file;

		List<ASTClass> classes;
		//Compute response
		try {
			classes = JDTVisitor.parse(file);
		} catch (Exception e){
			String response = "File not found!";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		// send response
		ObjectMapper json = ParsePars.getOutputFormat(parameters);
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(classes);
		he.getResponseHeaders().add("Content-Type","application/json");
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}

	@Override
	protected void printLog() {
		super.printLog();
		System.out.println("File served: " + lastFileServed);
	}
}
