package testing.datatype;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.types.DataTreeType;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MainDataType {

	MongoConnector db;

	public static final String _TEST_ = "/Users/giovanni/repository/java-xal/testing/src/test/resources/dataTypes";

	public static void main(String args[]) throws IOException {
		new MainDataType().run();
	}

	private void run() throws IOException {
		long start = System.currentTimeMillis();
		IndexingProject indexProject = new IndexingProject("test");
		int n = indexProject.indexProject(_TEST_, true);
		long end = System.currentTimeMillis();
		System.out.println("Indexed " + n + " files [" + (end - start) + "ms]");

		String file = _TEST_ + "/Square.java";
		Java2AST a = new Java2AST(file, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, file);
		ast.accept(v);

		DataTreeType d = new DataTreeType(v.listOfClasses.get(0));
		d.toString();
	}
}
