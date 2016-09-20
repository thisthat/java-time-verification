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
public class TestNotSync {

	final String DB_NAME = "TestNotSync";

	@Before
	public void setUp() throws Exception {
		MongoOptions.getInstance().setDbName(DB_NAME);
		MongoConnector.getInstance().drop();
		MongoConnector.getInstance().ensureIndexes();
	}

	@Test
	public void TestSameVarButNotAccessibleFromOutside() throws Exception {
		String f =  TestNotSync.class.getClassLoader().getResource("shouldNotSync/MagnetPlugin.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, f);
		ast.accept(v);
		ASTClass c = v.listOfClasses.get(0);

		//add the second method
		f =  TestNotSync.class.getClassLoader().getResource("shouldNotSync/MagnetURIHandlerImpl.java").getFile();
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
}
