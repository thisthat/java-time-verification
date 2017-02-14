package test;

import java.io.IOException;

/**
 * Created by giovanni on 14/02/2017.
 */
public class StartServer {

    public static void main(String[] args) throws IOException {
        int port = HttpServerConverter.port;
        int i = 0;
        while(i < args.length){
            String current = args[i++];
            switch (current){
                case "-port":
                    port = Integer.parseInt(args[i++]);
                    break;
            }
        }
        new StartServer().run(port);
    }
    public void run(int port) throws IOException {
        HttpServerConverter server = new HttpServerConverter(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }


}
