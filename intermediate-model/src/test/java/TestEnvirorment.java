import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.heuristic.*;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.structure.IndexData;
import intermediateModel.structure.*;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.JDTVisitor;
import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Triplet;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestEnvirorment {
    String filename = "env/AttributeTimeRelated.java";
    List<ASTClass> intemediateModel;

	public List<ASTClass> init(String filename) throws Exception {
		Java2AST a = new Java2AST( filename );
		a.convertToAST(Java2AST.VERSION.JDT);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		return v.listOfClasses;
	}

	@Test
	public void TestShowBug18() throws Exception {
		String filename = getClass().getClassLoader().getResource("AttributeTimeRelated.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		IndexingFile indexing = new IndexingFile();
		IndexData data = indexing.index(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertTrue(constraints.contains(new Triplet<>(
				14,
				"paused_on > 0 && started_on > 0",
				TimeoutResources.class
		)));

		assertEquals(constraints.size(), 1);

	}
}
