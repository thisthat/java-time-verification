package testsRoutes;

import com.fasterxml.jackson.databind.ObjectMapper;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.*;
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
public class TestOpenProject {

	static HttpServerConverter server;
	static String base_url;
	static String base_project;

	@Before
	public void setUp() throws Exception {
		server = new HttpServerConverter();
		base_url = "http://localhost:" + HttpServerConverter.getPort() + "/openProject";
		ClassLoader classLoader = TestOpenProject.class.getClassLoader();
		File file = new File(classLoader.getResource("progs/Attempt1.java").getFile());
		base_project = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/")) + "/";

		MongoOptions.getInstance().setDbName("tt");
		MongoConnector.getInstance().drop();
		MongoConnector.getInstance().ensureIndexes();
	}

	@After
	public void tearDown() throws Exception {
		MongoConnector.getInstance().close();
		server.stop();

	}

	@Test
	public void TestOpenProject() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		int status = 0;
		while(status == 0){
			httpclient = HttpClients.createDefault();
			httppost = new HttpPost("http://localhost:" + HttpServerConverter.getPort() + "/isProjectOpen");
			nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("name", "tt"));
			httppost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httppost);
			stream = response.getEntity().getContent();
			myString = IOUtils.toString(stream, "UTF-8");
			TestMains.Item itemWithOwner = new ObjectMapper().readValue(myString, TestMains.Item.class);
			status = itemWithOwner.getStatus();
		}
	}

	@Test
	public void TestOpenProjectMultpleRequests() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		response = httpclient.execute(httppost);
		stream = response.getEntity().getContent();
		myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		response = httpclient.execute(httppost);
		stream = response.getEntity().getContent();
		myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		int status = 0;
		while(status == 0){
			httpclient = HttpClients.createDefault();
			httppost = new HttpPost("http://localhost:" + HttpServerConverter.getPort() + "/isProjectOpen");
			nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("name", "tt"));
			httppost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httppost);
			stream = response.getEntity().getContent();
			myString = IOUtils.toString(stream, "UTF-8");
			TestMains.Item itemWithOwner = new ObjectMapper().readValue(myString, TestMains.Item.class);
			status = itemWithOwner.getStatus();
		}
	}

	@Test
	public void TestOpenProjectNoProto() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(400, response.getStatusLine().getStatusCode());

	}

	@Test
	public void TestOpenProjectNoPars() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		//nvps.add(new BasicNameValuePair("name", "tt"));
		//nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(406, response.getStatusLine().getStatusCode());
	}

	@Test
	public void TestOpenProjectNo1Par() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		//nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(400, response.getStatusLine().getStatusCode());
	}


	@Test
	public void TestOpenProjectNo1ParFail() throws Exception {
		MongoOptions.getInstance().setDbName("tt");
		MongoConnector.getInstance().drop();
		MongoConnector.getInstance().ensureIndexes();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		//nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(400, response.getStatusLine().getStatusCode());
	}

	@Test
	public void TestOpenProjectNo2Par() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		//nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(406, response.getStatusLine().getStatusCode());
	}

	@Test
	public void TestOpenProjectOptPars() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		nvps.add(new BasicNameValuePair("invalidCache", "1"));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		int status = 0;
		while(status == 0){
			httpclient = HttpClients.createDefault();
			httppost = new HttpPost("http://localhost:" + HttpServerConverter.getPort() + "/isProjectOpen");
			nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("name", "tt"));
			httppost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httppost);
			stream = response.getEntity().getContent();
			myString = IOUtils.toString(stream, "UTF-8");
			TestMains.Item itemWithOwner = new ObjectMapper().readValue(myString, TestMains.Item.class);
			status = itemWithOwner.getStatus();
		}
	}
	@Test
	public void TestOpenProjectOptPars2() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(base_url);
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("name", "tt"));
		nvps.add(new BasicNameValuePair("path", "file://" + base_project));
		nvps.add(new BasicNameValuePair("invalidCache", "0"));
		httppost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = httpclient.execute(httppost);
		InputStream stream = response.getEntity().getContent();
		String myString = IOUtils.toString(stream, "UTF-8");
		//System.out.println(myString);
		assertEquals(200, response.getStatusLine().getStatusCode());
		int status = 0;
		while(status == 0){
			httpclient = HttpClients.createDefault();
			httppost = new HttpPost("http://localhost:" + HttpServerConverter.getPort() + "/isProjectOpen");
			nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("name", "tt"));
			httppost.setEntity(new UrlEncodedFormEntity(nvps));
			response = httpclient.execute(httppost);
			stream = response.getEntity().getContent();
			myString = IOUtils.toString(stream, "UTF-8");
			TestMains.Item itemWithOwner = new ObjectMapper().readValue(myString, TestMains.Item.class);
			status = itemWithOwner.getStatus();
		}
	}

}
