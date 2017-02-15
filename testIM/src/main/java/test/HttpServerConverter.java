package test;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by giovanni on 14/02/2017.
 */
public class HttpServerConverter {
    static int port = 9000;
    private final int noOfThreads = 4;
    private final ExecutorService httpThreadPool;
    HttpServer server;
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
        server.createContext("/", new MiningChanges());
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
    }
    public void stop() {
        System.out.println("server stopped at " + port);
        server.stop(1);
        httpThreadPool.shutdownNow();
    }
}
