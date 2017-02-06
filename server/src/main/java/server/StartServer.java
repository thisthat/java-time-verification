package server;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
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
