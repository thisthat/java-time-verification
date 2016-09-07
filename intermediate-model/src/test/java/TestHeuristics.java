import IntermediateModelHelper.heuristic.definition.*;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
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
public class TestHeuristics {

	public List<ASTClass> init(String filename) throws Exception {
		Java2AST a = new Java2AST( filename );
		a.convertToAST(Java2AST.VERSION.JDT);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, filename);
		ast.accept(v);
		return v.listOfClasses;
	}

	@Test
	public void TestExportChangesJob() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/ExportChangesJob.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));


		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void TestFailoverTimeoutTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/FailoverTimeoutTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();



		assertTrue(check(
				103,
				"assertTrue(duration > 3000);",
				TimeoutResources.class,
				constraints
		));
		assertTrue(check(
				195,
				"TimeUnit.MILLISECONDS.sleep(Math.max(0, sleepMillis.addAndGet(-50)));",
				ThreadTime.class,
				constraints
		));

		assertEquals(constraints.size(), 2);

	}

	@Test
	public void TestJavaTimerExampleTask() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/JavaTimerExampleTask.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();


		assertTrue(check(
				16,
				"Thread.sleep(4000);",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				35,
				"timer.schedule(task, 0, 5000);",
				TimerType.class,
				constraints
		));
		assertTrue(check(
				38,
				"task.wait();",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				43,
				"Thread.sleep(10000);",
				ThreadTime.class,
				constraints
		));

		assertEquals(constraints.size(), 4);

	}

	@Test
	public void TestMCGroupImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/MCGroupImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();


		assertTrue(check(
				817,
				"socket.receive( packet );",
				SocketTimeout.class,
				constraints
		));

		assertEquals(constraints.size(), 1);

	}

	@Test
	public void TestProjectServiceImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/ProjectServiceImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();


		assertTrue(check(
				349,
				"Thread.sleep(500+((int)Math.random()*1000));",
				ThreadTime.class,
				constraints
		));
		assertEquals(constraints.size(), 1);

	}

	@Test
	public void TestSmallTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/SmallTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);
	}

	@Test
	public void TestsocketTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/socketTest.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();


		assertTrue(check(
				39,
				"DataOutputStream dos = new DataOutputStream( socket.getOutputStream());",
				SocketTimeout.class,
				constraints
		));
		assertEquals(constraints.size(), 1);


	}

	@Test
	public void TestTest() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/Test.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		List<Triplet<String,IASTStm,Class>> constraints;
		//First class
		ah.analyze(cs.get(0));
		constraints = ah.getTimeConstraint();


		assertTrue(check(
				86,
				"Thread.sleep(5000);",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				205,
				"wait();",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				232,
				"createTopologyThread.sleep(1000);",
				ThreadTime.class,
				constraints
		));
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
		assertTrue(check(
				290,
				"Thread.sleep(5000);",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				293,
				"Thread.sleep(5000);",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				302,
				"Thread.sleep(_class.SleepTimeout);",
				ThreadTime.class,
				constraints
		));
		assertTrue(check(
				308,
				"Thread.join();",
				ThreadTime.class,
				constraints
		));


	}

	@Test
	public void TesttestLambdas() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/testLambdas.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));
		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);

	}

	@Test
	public void TestTimerEvent() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/TimerEvent.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();
		assertEquals(constraints.size(), 0);

	}

	@Test
	public void TestUPnPImpl() throws Exception {
		String filename = getClass().getClassLoader().getResource("examples/UPnPImpl.java").getFile();
		List<ASTClass> cs = init(filename);
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(cs.get(0));

		List<Triplet<String,IASTStm,Class>> constraints = ah.getTimeConstraint();


		assertTrue(check(
				612,
				"SystemTime.getMonotonousTime() - last_fail < period",
				TimeoutResources.class,
				constraints
		));
		assertTrue(check(
				864,
				"PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), \"UTF8\"));",
				SocketTimeout.class,
				constraints
		));
		assertEquals(constraints.size(), 2);

	}

	private boolean check(int line, String code, Class _class, List<Triplet<String, IASTStm, Class>> constraints ){
		boolean flag = false;
		for(Triplet<String, IASTStm, Class> c : constraints){
			if(		c.getValue1().getCode().equals(code) &&
					c.getValue1().getLine() == line &&
					c.getValue2().equals(_class)){
				flag = true;
			}
		}
		return flag;
	}

}
