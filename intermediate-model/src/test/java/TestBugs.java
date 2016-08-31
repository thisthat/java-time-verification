import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTHiddenClass;
import intermediateModel.structure.ASTIf;
import intermediateModel.structure.ASTMethod;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import parser.Java2AST;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestBugs {

	@Test
	public void bugHiddenClasses() throws Exception {
		//first method
		String f =  TestBugs.class.getClassLoader().getResource("bugs/HiddenClass.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		//one class
		assertEquals(v.listOfClasses.size(), 1);
		//two methods
		assertEquals(c.getMethods().size(), 2);
		//two hidden classes
		final int[] n_hidden = {0};
		final ASTHiddenClass[] hidden = { null, null};
		c.visit(new DefaultASTVisitor(){
			@Override
			public void enterASTHiddenClass(ASTHiddenClass astHiddenClass) {
				hidden[n_hidden[0]] = astHiddenClass;
				n_hidden[0]++;
			}
		});
		assertEquals(n_hidden[0], 2);
		//first hidden 3 methods
		assertEquals(hidden[0].getMethods().size(), 3);
		//second hidden 1 method
		assertEquals(hidden[1].getMethods().size(), 1);
	}

	@Test
	public void bugHiddenClassesStmsToCorrectMethod() throws Exception {
		//first method
		String f =  TestBugs.class.getClassLoader().getResource("bugs/Strange.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast,f);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);

	}

	@Test
	public void notWellParsed() throws Exception {
		String f =  TestBugs.class.getClassLoader().getResource("bugs/NotWellParsed.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast,f);
		ast.accept(v);
		//we have only one class
		ASTClass c = v.listOfClasses.get(0);
		assertEquals(c.getMethods().size(), 1);
		IASTMethod m = c.getMethods().get(0);
		assertEquals(m.getStms().size(), 1);
		assertEquals(m.getStms().get(0).getClass(), ASTIf.class);
		ASTIf elm = (ASTIf) m.getStms().get(0);
		assertEquals(elm.getIfBranch().getStms().size(), 6);
	}
}
