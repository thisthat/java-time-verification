package server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.HttpServerConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class EchoGetHandler implements HttpHandler {



	@Override
	public void handle(HttpExchange he) throws IOException {
		// parse request
		Map<String, String> parameters = new HashMap<>();
		URI requestedUri = he.getRequestURI();
		String query = requestedUri.getRawQuery();
		HttpServerConverter.parseQuery(query, parameters);

		// send response
		String response = "";
		for (String key : parameters.keySet())
			response += key + " = " + parameters.get(key) + "\n";
		he.sendResponseHeaders(200, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.toString().getBytes());

		os.close();
	}

}
