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

		assertEquals(constraints.size(), 2);

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

		assertEquals(constraints.size(), 4);

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



		assertTrue(constraints.contains(new Triplet<>(
				817,
				"socket.receive( packet );",
				SocketTimeout.class
		)));

		assertEquals(constraints.size(), 1);

	}

	@Test
	public void TestProjectServiceImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("ProjectServiceImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();



		assertTrue(constraints.contains(new Triplet<>(
				349,
				"Thread.sleep(500+((int)Math.random()*1000));",
				ThreadTime.class
		)));

		assertEquals(constraints.size(), 1);

	}

	@Test
	public void TestSmallTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("SmallTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 0);
	}

	@Test
	public void TestsocketTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("socketTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();



		assertTrue(constraints.contains(new Triplet<>(
				39,
				"DataOutputStream dos = new DataOutputStream( socket.getOutputStream());",
				SocketTimeout.class
		)));
		assertEquals(constraints.size(), 1);


	}

	@Test
	public void TestTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("Test.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		List<Triplet<Integer,String,Class>> constraints;

		//First class
		ah.analyze(cs.get(0));
		constraints = ah.getTimeConstraint();


		assertTrue(constraints.contains(new Triplet<>(
				86,
				"Thread.sleep(5000);",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				205,
				"wait();",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				232,
				"createTopologyThread.sleep(1000);",
				ThreadTime.class
		)));
		assertEquals(constraints.size(), 3);

		//Second class
		ah.analyze(cs.get(1));
		constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);

		//Third class
		ah.analyze(cs.get(2));
		constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);

		//Fourth class
		ah.analyze(cs.get(3));
		constraints = ah.getTimeConstraint();
		assertTrue(constraints.contains(new Triplet<>(
				290,
				"Thread.sleep(5000);",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				293,
				"Thread.sleep(5000);",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				302,
				"Thread.sleep(_class.SleepTimeout);",
				ThreadTime.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				308,
				"Thread.join();",
				ThreadTime.class
		)));


	}

	@Test
	public void TesttestLambdas() throws Exception {
		String filename = getClass().getClassLoader().getResource("testLambdas.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 0);

	}

	@Test
	public void TestTimerEvent() throws Exception {
		String filename = getClass().getClassLoader().getResource("TimerEvent.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();

		assertEquals(constraints.size(), 0);

	}

	@Test
	public void TestUPnPImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("UPnPImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<Integer,String,Class>> constraints = ah.getTimeConstraint();



		assertTrue(constraints.contains(new Triplet<>(
				612,
				"SystemTime.getMonotonousTime() - last_fail < period",
				TimeoutResources.class
		)));
		assertTrue(constraints.contains(new Triplet<>(
				864,
				"PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), \"UTF8\"));",
				SocketTimeout.class
		)));
		assertEquals(constraints.size(), 2);

	}


}
