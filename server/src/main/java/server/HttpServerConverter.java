package server;

import com.sun.net.httpserver.HttpServer;
import server.handler.*;
import server.handler.test.echoGet;
import server.handler.test.echoHeader;
import server.handler.test.echoPost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class HttpServerConverter {
	static int port = 9000;
	private final int noOfThreads = 4;
	private final ExecutorService httpThreadPool;
	HttpServer server;
	private static boolean _debug;

	private static Map<String,String> lastParameters = new HashMap<>();

	public static int getPort() {
		return port;
	}

	public HttpServerConverter() throws IOException {
		this(port);
	}

	public HttpServerConverter(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		System.out.println("server started at " + port);


		httpThreadPool = Executors.newFixedThreadPool(this.noOfThreads);

		//Test urls
		server.createContext("/", new root());
		server.createContext("/test/echoHeader", new echoHeader());
		server.createContext("/test/echoGet", new echoGet());
		server.createContext("/test/echoPost", new echoPost());


		//Get all java files from a project
		server.createContext("/getAllFiles", new getAllFiles());
		server.createContext("/getFile", new getFile());


		openProject op = new openProject();
		//Start project index
		server.createContext("/openProject", op);
		server.createContext("/isProjectOpen", new isProjectOpen());
		server.createContext("/getFilesByType", new getFilesByType());
		server.createContext("/getThreads", new getThreads());
		server.createContext("/getStatus", new getStatus(op));
		server.createContext("/getMains", new getMains());

		server.setExecutor(httpThreadPool);
		server.start();
	}

	public static void parseQuery(String query, Map<String,String> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0],
							System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1],
							System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);

					}
				} else {
					parameters.put(key, value);
				}
			}
		}
		lastParameters = parameters;
	}

	public static Map<String, String> getLastParameters() {
		return lastParameters;
	}

	public static boolean isDebugActive() {
		return _debug;
	}

	public void stop() {
		System.out.println("server stopped at " + port);
		server.stop(1);
		httpThreadPool.shutdownNow();
	}

	public void setDebug(boolean debug) {
		_debug = debug;
	}
}
