import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.creation.IM2PCFG;
import PCFG.structure.PCFG;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestSynchronization {

	final String DB_NAME = "TestSynchronization";

	@Before
	public void setUp() throws Exception {
		MongoOptions.getInstance().setDbName(DB_NAME);
		MongoConnector.getInstance().drop();
		MongoConnector.getInstance().ensureIndexes();
	}

	@Test
	public void TestSameVarButNotAccessibleFromOutside() throws Exception {
		String f =  TestSynchronization.class.getClassLoader().getResource("shouldNotSync/MagnetPlugin.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f =  TestSynchronization.class.getClassLoader().getResource("shouldNotSync/MagnetURIHandlerImpl.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("_downloadSupport",
				Arrays.asList("MagnetPluginProgressListener, byte[], String, InetSocketAddress[], long, int".split(", "))
		), true);
		p.addClass(c1, c1.getMethodBySignature("process",
				Arrays.asList("String, BufferedReader, OutputStream".split(", "))
		), true);
		MongoConnector.getInstance().ensureIndexes();
		PCFG g = p.buildPCFG();

		assertEquals(g.getSyncNodes().size(), 12 + 7 );
		assertEquals(g.getCFG().size(), 2 );
		assertEquals(g.getESync().size(), 0 );

	}

	@Test
	public void TestSameClassOnThis() throws Exception {
		String f =  TestSynchronization.class.getClassLoader().getResource("airavata/WaitDialog.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f =  TestSynchronization.class.getClassLoader().getResource("airavata/WaitDialog.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("show",
				Arrays.asList()
		), true);
		p.addClass(c1, c1.getMethodBySignature("show",
				Arrays.asList()
		), true);
		MongoConnector.getInstance().ensureIndexes();
		PCFG g = p.buildPCFG();

		assertEquals(g.getSyncNodes().size(), 1 + 1 );
		assertEquals(g.getCFG().size(), 2 );
		assertEquals(g.getESync().size(), 1 );

	}

	@Test
	public void TestSameClassOnDotClass() throws Exception {
		String f =  TestSynchronization.class.getClassLoader().getResource("airavata/WorkflowEnactmentService.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f =  TestSynchronization.class.getClassLoader().getResource("airavata/WorkflowEnactmentService.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("getInstance",
				Arrays.asList()
		), true);
		p.addClass(c1, c1.getMethodBySignature("getInstance",
				Arrays.asList()
		), true);
		MongoConnector.getInstance().ensureIndexes();
		PCFG g = p.buildPCFG();

		assertEquals(g.getSyncNodes().size(), 1 + 1 );
		assertEquals(g.getCFG().size(), 2 );
		assertEquals(g.getESync().size(), 1 );

	}

	@Test
	public void TestSameOutsideVariable() throws Exception {
		String f =  TestSynchronization.class.getClassLoader().getResource("airavata/HPCPullMonitor.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f = TestSynchronization.class.getClassLoader().getResource("airavata/CommonUtils.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("run",
				Arrays.asList()
		), true);
		p.addClass(c1, c1.getMethodBySignature("addMonitortoQueue",
				Arrays.asList("BlockingQueue<UserMonitorData>, MonitorID, JobExecutionContext".split(", "))
		), true);
		MongoConnector.getInstance().ensureIndexes();
		PCFG g = p.buildPCFG();

		assertEquals(g.getSyncNodes().size(), 1 + 1 );
		assertEquals(g.getCFG().size(), 2 );
		assertEquals(g.getESync().size(), 1 );
	}

	@Test
	public void TestSameNameDifferentClasses() throws Exception {
		String f =  TestSynchronization.class.getClassLoader().getResource("airavata/PredicatedTaskRunner_co.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f = TestSynchronization.class.getClassLoader().getResource("airavata/PredicatedTaskRunner_wf.java").getFile();
		a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		ast = a.getContextJDT();
		v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c1 = v.listOfClasses.get(0);

		IM2PCFG p = new IM2PCFG();
		p.addClass(c, c.getMethodBySignature("addIdleTask",
				Arrays.asList()
		), true);
		p.addClass(c1, c1.getMethodBySignature("addIdleTask",
				Arrays.asList()
		), true);
		MongoConnector.getInstance().ensureIndexes();
		PCFG g = p.buildPCFG();

		assertEquals(g.getSyncNodes().size(), 1 + 1 );
		assertEquals(g.getCFG().size(), 2 );
		assertEquals(g.getESync().size(), 0 );
	}



}
