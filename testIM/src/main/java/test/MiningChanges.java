package test;

import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTSynchronized;
import IntermediateModel.visitors.DefaultASTVisitor;
import IntermediateModel.visitors.creation.JDTVisitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;

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

        if(!parameters.containsKey("data") || !parameters.containsKey("old") || !parameters.containsKey("new")){
            String response = "[]";
            he.getResponseHeaders().add("Content-Type","application/json");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
        String changesJson = parameters.get("data");
        String _old = parameters.get("old");
        String _new = parameters.get("new");
        ObjectMapper mapper = new ObjectMapper();

        List<ASTClass> oldClasses = JDTVisitor.parse(_old);
        List<ASTClass> newClasses = JDTVisitor.parse(_new);
        List<LinkedHashMap> changes = mapper.readValue(changesJson, List.class);


        List<Boolean> outputVals = new ArrayList<>();
        for(LinkedHashMap c : changes){
            Boolean flag = false;
            int start = (int) c.get("start");
            int end = (int) c.get("end");
            if (c.get("type").equals("DELETE")) {
                flag = check(start, end, oldClasses);
            } else {
                flag = check(start, end, newClasses);
            }
            outputVals.add(flag);
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

    private static Boolean check(int start, int end, List<ASTClass> cl){
        final Boolean[] ret = {false};
        for(ASTClass _class : cl){
            _class.visit(new DefaultASTVisitor(){
                @Override
                public void enterASTMethod(ASTMethod elm) {
                    //we have an edit on a synchronized method
                    if(elm.getStart() <= start && elm.getEnd() >= end)
                        if(elm.isSyncronized()) ret[0] = true;
                }

                @Override
                public void enterASTSynchronized(ASTSynchronized elm) {
                    //we have an edit on a synchronized block
                    if(elm.getStart() <= start && elm.getEnd() >= end)
                        ret[0] = true;
                }
            });
        }
        return ret[0];
    }

}
