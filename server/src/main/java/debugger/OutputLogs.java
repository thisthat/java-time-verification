package debugger;

import com.sun.net.httpserver.HttpExchange;
import server.handler.middleware.BaseRoute;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputLogs extends BaseRoute {
    private static OutputLogs instance = null;
    private List<String> logs = new ArrayList<>();

    public static synchronized OutputLogs getInstance(){
        if(instance == null) {
            instance = new OutputLogs();
        }
        return instance;
    }

    public void add(String msg){
        logs.add(msg);
    }

    @Override
    public void handleConnection(HttpExchange he) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><textarea style=\"width:100%;height:100%;\">");
        for(String m : logs){
            sb.append(m);
            sb.append("\n");
        }
        sb.append("</textarea></body></html>");
        String response = sb.toString();
        he.sendResponseHeaders(400, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }
}
