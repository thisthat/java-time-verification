import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import server.HttpServerConverter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestGetFile {

	static HttpServerConverter server;
	static String base_url;
	static String base_project;

	@BeforeClass
	public static void setUp() throws Exception {
		server = new HttpServerConverter();
		base_url = "http://localhost:" + HttpServerConverter.getPort();
		ClassLoader classLoader = TestGetFile.class.getClassLoader();
		File file = new File(classLoader.getResource("progs/Attempt1.java").getFile());
		base_project = file.getAbsolutePath();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void TestGetFile() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url + "/getFile");
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("filePath", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());

	}
	@Test
	public void TestGetFileNoFileURI() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url + "/getFile");
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("filePath", "" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		assertEquals(400, response.getStatusLine().getStatusCode());
	}
	@Test
	public void TestGetFileNoFile() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url + "/getFile");
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("filePath", "file://C:/file.java"));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		assertEquals(400, response.getStatusLine().getStatusCode());
	}
	@Test
	public void TestGetFileNoParam() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url + "/getFile");
		List<NameValuePair> nvps = new ArrayList<>();
		//nvps.add(new BasicNameValuePair("filePath", "file://C:/file.java"));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		assertEquals(400, response.getStatusLine().getStatusCode());
	}
}
