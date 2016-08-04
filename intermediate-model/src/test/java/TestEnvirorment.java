import IntermediateModelHelper.heuristic.definition.*;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.structure.IndexData;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Triplet;
import org.junit.Test;
import parser.Java2AST;

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

		List<Triplet<String, IASTStm, Class>> constraints = ah.getTimeConstraint();

		assertTrue(check(
				14,
				"paused_on > 0 && started_on > 0",
				TimeoutResources.class,
				constraints
		));

		assertEquals(constraints.size(), 1);

	}

	private boolean check(int line, String message, Class _class, List<Triplet<String, IASTStm, Class>> constraints ){
		boolean flag = false;
		for(Triplet<String, IASTStm, Class> c : constraints){
			if(c.getValue0().equals(message) &&
			   c.getValue1().getLine() == line &&
			   c.getValue2().equals(_class)){
				flag = true;
			}
		}
		return flag;
	}
}
