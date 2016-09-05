package testing.sendEmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {
	public static void main(String[] args) throws IOException {
		String a="http://sensarisvolti.altervista.org/send_email.php?msg=package%200%20finish";
		URL url = new URL(a);
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		br.read();
	}
}
