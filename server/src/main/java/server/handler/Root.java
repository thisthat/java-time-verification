package server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.HttpServerConverter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Root implements HttpHandler {

	@Override
	public void handle(HttpExchange he) throws IOException {
		String response = "The connection works. But your request was not correct.";
		he.sendResponseHeaders(400, response.length());
		OutputStream os = he.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
