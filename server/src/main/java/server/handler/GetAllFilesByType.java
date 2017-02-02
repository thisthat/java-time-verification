package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.FileUtils;
import server.HttpServerConverter;

import java.io.*;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GetAllFilesByType implements HttpHandler {

	String par1 = "projectPath";
	String par2 = "skipTest";
	String par3 = "type";


	@Override
	public void handle(HttpExchange he) throws IOException {
		// parse request
		Map<String, String> parameters = new HashMap<>();
		InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		HttpServerConverter.parseQuery(query, parameters);

		//validate input: expect 3 pars projectPath, skipTest, type
		boolean flag = true;
		if(!parameters.containsKey(par1)){
			flag = false;
		}
		if(!parameters.containsKey(par2)){
			flag = false;
		}
		if(!parameters.containsKey(par3)){
			flag = false;
		}
		if(!flag){
			String response = "Incorrect format of parameters.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		boolean skipTest = parameters.get(par2).equals("1");
		String base_path = parameters.get(par1);
		String type = parameters.get(par3);
		if(!base_path.startsWith("file://")){
			String response = "Unsupported protocol. atm only file:// is supported.";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		base_path = base_path.replace("file://","");
		//Compute response
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = null;
		try{
			files = FileUtils.listFiles(
					dir,
					filter,
					true
			);
		}
		catch (Exception e){
			String response = "Directory " + base_path + " does not exists or it cannot be opened";
			he.sendResponseHeaders(400, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			return;
		}
		Iterator i = files.iterator();
		List<String> outputFiles = new ArrayList<>();

		while (i.hasNext()) {

			String filename = ((File)i.next()).getAbsolutePath();
			if(skipTest && filename.contains("/test")){
				continue;
			}
			outputFiles.add(filename);
		}


		// send response
		ObjectMapper json = new ObjectMapper();
		String response = json.writeValueAsString(outputFiles);
		he.getResponseHeaders().add("Content-Type","application/json");
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}
}
