package cmd;

import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import server.cmd.ArgumentParser;
import testsRoutes.TestGetAllFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestCMD {

	private static String base_project;
	private static ByteArrayOutputStream bo = new ByteArrayOutputStream();
	private static PrintStream output = new PrintStream(bo, true);
	private static PrintStream oldOut = System.out;

	@BeforeClass
	public static void setUp() throws Exception {
		ClassLoader classLoader = TestGetAllFile.class.getClassLoader();
		File file = new File(classLoader.getResource("progs/Attempt1.java").getFile());
		base_project = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/")) + "/";
		oldOut = System.out;
		System.setOut(output);
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void TestOpenProjectNoPath() throws IOException {
		String[] args = {"-name", "t", "-cmd", "openProject"};
		ArgumentParser ap = ArgumentParser.parse(args);
		ap.call();
		String data = new String(bo.toByteArray(), StandardCharsets.UTF_8);
		assertTrue(data.startsWith("Incorrect format"));
	}

	@Test
	public void TestOpenProject() throws IOException {
		String[] args = {"-name", "t", "-cmd", "openProject", "-path", "file://" + base_project};
		ArgumentParser ap = ArgumentParser.parse(args);
		ap.call();
		String data = new String(bo.toByteArray(), StandardCharsets.UTF_8);
		assertFalse(data.startsWith("Incorrect format"));
	}

	@Test
	public void TestClean() throws IOException {
		TestOpenProject();
		String[] args = {"-name", "t", "-cmd", "clean"};
		ArgumentParser ap = ArgumentParser.parse(args);
		ap.call();
		String data = new String(bo.toByteArray(), StandardCharsets.UTF_8);
		assertFalse(data.startsWith("Incorrect format"));
	}
}
