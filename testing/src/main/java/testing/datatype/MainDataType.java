package testing.datatype;

import IntermediateModelHelper.indexing.DataTreeType;
import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MainDataType {

	MongoConnector db;

	public static final String _TEST_ = "/Users/giovanni/repository/java-xal/testing/src/test/resources/dataTypes";

	public static void main(String args[]) throws IOException, ParseErrorsException {
		new MainDataType().run();
	}

	private void run() throws IOException, ParseErrorsException {
		long start = System.currentTimeMillis();
		IndexingProject indexProject = new IndexingProject("test");
		int n = indexProject.indexProject(_TEST_, true);
		long end = System.currentTimeMillis();
		System.out.println("Indexed " + n + " files [" + (end - start) + "ms]");

		String file = _TEST_ + "/Square.java";
		Java2AST a = new Java2AST(file, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);

		DataTreeType d = new DataTreeType(v.listOfClasses.get(0));
		d.toString();
	}
}
