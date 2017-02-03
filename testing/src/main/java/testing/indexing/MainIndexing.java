package testing.indexing;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class MainIndexing {

	MongoConnector db;

	public static final String VUZE = "/Users/giovanni/repository/java-xal/intermediate-model/src/test/resources/vuze";

	public static void main(String args[]) throws IOException {
		new MainIndexing().run1();
	}

	private void run() throws IOException {
		long start = System.currentTimeMillis();
		IndexingProject indexProject = new IndexingProject("vuze");
		int n = indexProject.indexProject(VUZE, true);
		n = n + indexProject.indexSyncBlock(VUZE, false);
		long end = System.currentTimeMillis();
		System.out.println("Indexed " + n + " files [" + (end - start) + "ms]");
	}

	private void run1() throws IOException {
		long start = System.currentTimeMillis();
		IndexingProject indexProject = new IndexingProject("abcd");
		String folder = MainIndexing.class.getClassLoader().getResource("testSyncExpr/Calculator.java").getPath();
		folder = folder.substring(0, folder.lastIndexOf("/"));
		int n = indexProject.indexProject(folder, true);
		n = n + indexProject.indexSyncBlock(folder, false);
		long end = System.currentTimeMillis();
		System.out.println("Indexed " + n + " files [" + (end - start) + "ms]");
	}
}
