import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import parser.Java2AST;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestBugs {

	@Test
	public void name() throws Exception {
		//first method
		String f =  TestBugs.class.getClassLoader().getResource("bugs/HiddenClass.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
	}
}
