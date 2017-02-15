package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.creation.JDTVisitor;
import server.HttpServerConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GetFile implements HttpHandler {
	String par1 = "filePath";

	@Override
	public void handle(HttpExchange he) throws IOException {
		// parse request
		Map<String, String> parameters = new HashMap<>();
		InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		HttpServerConverter.parseQuery(query, parameters);

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
		if(!base_path.startsWith("file://")){
			String response = "Unsupported protocol. atm only file:// is supported.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		base_path = base_path.replace("file://","");
		List<ASTClass> classes;
		//Compute response
		try {
			classes = JDTVisitor.parse(base_path);
		} catch (Exception e){
			String response = "File not found!";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		// send response
		ObjectMapper json = new ObjectMapper();
		json.enable(SerializationFeature.INDENT_OUTPUT);
		String response = json.writeValueAsString(classes.get(0));
		he.getResponseHeaders().add("Content-Type","application/json");
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}
}
