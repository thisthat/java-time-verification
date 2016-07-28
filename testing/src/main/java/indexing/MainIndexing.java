package indexing;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import parser.exception.ParseErrorsException;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

public class MainIndexing {

	MongoConnector db;

	public static final String VUZE = "/Users/giovanni/repository/java-xal/intermediate-model/src/test/resources/vuze";

	public static void main(String args[]) throws IOException, ParseErrorsException {
		new MainIndexing().run();
	}

	private void run() throws IOException, ParseErrorsException {
		IndexingProject indexProject = new IndexingProject("vuze");
		int n = indexProject.indexProject(VUZE);
		System.out.println("Indexed " + n + " files");
	}
}
