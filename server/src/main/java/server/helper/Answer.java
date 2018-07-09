package server.helper;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class Answer {

    static String version;
    static{
        version = String.format("{ \"v\": \"%s\", \"b\": \"%s\"},", PropertiesFileReader.getGitSha1(), PropertiesFileReader.getBranch());
    }

    public static void SendMessage(String response, HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Content-Type","application/json");
        he.sendResponseHeaders(200, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
