package server;

import server.cmd.ArgumentParser;
import server.helper.PropertiesFileReader;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CommandLine {

	static boolean debug = false;

	public static void main(String[] args) throws Exception {
		System.out.println("Version : " + PropertiesFileReader.getInfo());
		ArgumentParser ap = ArgumentParser.parse(args);
		ap.call();
	}
}
