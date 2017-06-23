import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTLabel;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.NotYetImplemented;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Pair;
import org.junit.Test;
import parser.Java2AST;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestCreationIM {
	@Test
	public void TestDeclarationVariable() throws Exception {
		String f = TestCreationIM.class.getClassLoader().getResource("env/CreateListDeclaredVar.java").getFile();
		ASTClass c = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/"))).get(0);
		IASTMethod m = c.getAllMethods().get(0);
		m.setDeclaredVars();
		assertEquals("Dequeue", m.getName());
		assertEquals(4, m.getDeclaredVar().size());
		//System.out.println(Arrays.toString(m.getDeclaredVar().toArray()));
	}

	@Test
	public void TestDeclarationAttribute() throws Exception {
		String f = TestCreationIM.class.getClassLoader().getResource("env/ConcQueue.java").getFile();
		ASTClass c = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/"))).get(0);
		assertEquals(2, c.getAttributes().size());
	}

	@Test
	public void TestASTLabel() throws Exception {
		ClassLoader classLoader = TestCreationIM.class.getClassLoader();
		String file = classLoader.getResource("annotations/Label.java").getFile();
		List<ASTClass> cs = JDTVisitor.parse(file, file.substring(0, file.lastIndexOf("/")));
		assertEquals(1, cs.size());
		ASTClass c = cs.get(0);
		final int[] nLabel = {0};
		c.visit(new DefaultASTVisitor() {
			@Override
			public void enterASTLabel(ASTLabel elm) {
				nLabel[0]++;
				assertEquals("mywhile", elm.getLabel());
			}
		});
		assertEquals(1, nLabel[0]);
	}
}
