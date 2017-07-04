import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTHiddenClass;
import intermediateModel.structure.ASTIf;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.heuristic.definition.ImplicitTimeComparisonApplication;
import intermediateModelHelper.indexing.IndexingSyncBlock;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import intermediateModelHelper.indexing.structure.IndexSyncBlock;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestImplicitTimeComparison {

	ASTClass c;
	List<IASTMethod> m = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		String f =  TestImplicitTimeComparison.class.getClassLoader().getResource("implicitTimeComparison/Example.java").getFile();
		List<ASTClass> cc = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/")));
		c = cc.get(0);
		m = c.getMethods();
	}

	@Test
	public void ClockTest() throws Exception {
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ImplicitTimeComparisonApplication.class);
		ah.set__DEBUG__(false);
		ah.analyze(c);
		assertEquals(1, ah.getTimeConstraint().size());
	}
}
