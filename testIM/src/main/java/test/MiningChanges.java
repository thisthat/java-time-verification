package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giovanni on 14/02/2017.
 */
public class MiningChanges implements HttpHandler {
    @Override
    public void handle(HttpExchange he) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        HttpServerConverter.parseQuery(query, parameters);

        List<Boolean> outputVals = new ArrayList<>();
        for(int i = 0; i <10; i ++){
            outputVals.add((i & 1) == 0);
        }

        // send response
        ObjectMapper json = new ObjectMapper();
        String response = json.writeValueAsString(outputVals);
        he.getResponseHeaders().add("Content-Type","application/json");
        he.sendResponseHeaders(200, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
