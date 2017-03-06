package server.handler.middleware;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.HttpServerConverter;

import java.io.IOException;

/**
 * Created by giovanni on 03/03/2017.
 */
public abstract class BaseRoute implements HttpHandler {

    public BaseRoute(){
    }

    public void handle(HttpExchange he) throws IOException {
        handleConnection(he);
        if(HttpServerConverter.isDebugActive()){
            printLog();
        }
    }

    protected abstract void handleConnection(HttpExchange he) throws IOException;

    protected void printLog(){
        String route =  getClass().getSimpleName();
        System.out.println("Serving " + route);
    }
}
