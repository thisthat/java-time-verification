import IntermediateModelHelper.heuristic.*;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Triplet;
import org.junit.Test;
import parser.Java2AST;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestHeuristics {

	public List<ASTClass> init(String filename) throws Exception {
		Java2AST a = new Java2AST( filename );
		a.convertToAST(Java2AST.VERSION.JDT);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		return v.listOfClasses;
	}

	@Test
	public void TestExportChangesJob() throws Exception {
		String filename = getClass().getClassLoader().getResource("ExportChangesJob.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		String s = Arrays.toString( ah.getTimeConstraint().toArray() );
		System.err.println(s);

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void TestFailoverTimeoutTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("FailoverTimeoutTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 2);

		assertTrue(constraints.contains(new Triplet<>(
				103,
				"assertTrue(duration > 3000);",
				TimeoutResources.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				195,
				"TimeUnit.MILLISECONDS.sleep(Math.max(0, sleepMillis.addAndGet(-50)));",
				ThreadTime.class
		)));

	}

	@Test
	public void TestJavaTimerExampleTask() throws Exception {
		String filename = getClass().getClassLoader().getResource("JavaTimerExampleTask.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 4);

		assertTrue(constraints.contains(new Triplet<>(
				16,
				"Thread.sleep(4000);",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				35,
				"timer.schedule(task, 0, 5000);",
				TimerType.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				38,
				"task.wait();",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				43,
				"Thread.sleep(10000);",
				ThreadTime.class
		)));

	}

	@Test
	public void TestMCGroupImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("MCGroupImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 1);

		assertTrue(constraints.contains(new Triplet<>(
				817,
				"socket.receive( packet );",
				SocketTimeout.class
		)));


	}

}
