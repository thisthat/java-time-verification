package server;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class StartServer {

	public static void main(String[] args) throws IOException {
		new StartServer().run();
	}

	public void run() throws IOException {
		HttpServerConverter server = new HttpServerConverter();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
	}
}
