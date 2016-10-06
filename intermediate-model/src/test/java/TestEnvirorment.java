import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.envirorment.EnvExtended;
import IntermediateModelHelper.heuristic.definition.*;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexData;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Triplet;
import org.junit.Before;
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

	@Before
	public void setUp() throws Exception {
		MongoOptions.getInstance().setDbName("testEnv");
		MongoConnector.getInstance().drop();

	}

	public List<ASTClass> init(String filename) throws Exception {
		Java2AST a = new Java2AST( filename );
		a.convertToAST(Java2AST.VERSION.JDT);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast, filename);
		ast.accept(v);
		return v.listOfClasses;
	}

	@Test
	public void TestShowBug18() throws Exception {
		String filename = getClass().getClassLoader().getResource("env/AttributeTimeRelated.java").getFile();
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

	@Test
	public void TestEnvExtended() throws Exception {
		IndexingProject indexing = new IndexingProject();
		String directory = getClass().getClassLoader().getResource("env/AttributeTimeRelated.java").getFile();
		String base_path = directory.substring(0, directory.lastIndexOf("/")+1);
		indexing.setSkipTest(false);
		indexing.indexProject(base_path);
		MongoConnector.getInstance().ensureIndexes();
		final Env[] out = new Env[1];
		ParseIM getEnv = new ParseIM (){
			@Override
			public void start(ASTClass c) {
				super.start(c);
				Env e = super.createBaseEnv(c);
				e.setPrev( EnvExtended.getExtendedEnv(c));
				out[0] = e;
			}
		};

		List<ASTClass> cs;
		EnvExtended e;
		cs = JDTVisitor.parse( getClass().getClassLoader().getResource("env/Shape.java").getFile());
		getEnv.start(cs.get(0));
		assertTrue(out[0].getPrev().getPrev() == null);
		assertTrue(out[0].existVarName("x"));
		assertTrue(out[0].existVarName("y"));
		assertTrue(out[0].getVar("x").getType().equals("int"));
		assertTrue(out[0].getVar("y").getType().equals("int"));
		e = out[0].getExtendedEnv();
		assertEquals(e.getClassName(), "Object");
		assertEquals(e.getPackageName(), "");

		cs = JDTVisitor.parse( getClass().getClassLoader().getResource("env/Rectangle.java").getFile());
		getEnv.start(cs.get(0));
		assertTrue(out[0].getPrev() instanceof EnvExtended);
		assertTrue(out[0].isInherited("x"));
		assertTrue(out[0].isInherited("y"));
		assertTrue(out[0].getPrev().existVarName("x"));
		assertTrue(out[0].getPrev().existVarName("y"));
		assertTrue(out[0].getPrev().getVar("x").getType().equals("int"));
		assertTrue(out[0].getPrev().getVar("y").getType().equals("int"));
		assertTrue(out[0].existVarName("length"));
		assertTrue(out[0].existVarName("width"));
		assertTrue(out[0].getVar("length").getType().equals("double"));
		assertTrue(out[0].getVar("length").getType().equals("double"));
		e = out[0].getExtendedEnv();
		assertEquals(e.getClassName(), "Shape");
		assertEquals(e.getPackageName(), "");
		e = out[0].getExtendedEnv("x");
		assertEquals(e.getClassName(), "Shape");
		assertEquals(e.getPackageName(), "");

		cs = JDTVisitor.parse( getClass().getClassLoader().getResource("env/Square.java").getFile());
		getEnv.start(cs.get(0));
		assertTrue(out[0].getPrev() instanceof EnvExtended);
		assertTrue(out[0].getPrev().getPrev() instanceof EnvExtended);
		assertTrue(out[0].isInherited("x"));
		assertTrue(out[0].isInherited("y"));
		assertTrue(out[0].isInherited("length"));
		assertTrue(out[0].isInherited("width"));
		assertTrue(out[0].existVarName("x"));
		assertTrue(out[0].existVarName("y"));
		assertTrue(out[0].getVar("x").getType().equals("int"));
		assertTrue(out[0].getVar("y").getType().equals("int"));
		assertTrue(out[0].existVarName("length"));
		assertTrue(out[0].existVarName("width"));
		assertTrue(out[0].getVar("length").getType().equals("double"));
		assertTrue(out[0].getVar("length").getType().equals("double"));
		assertTrue(out[0].existVarName("size"));
		assertTrue(out[0].getVar("size").getType().equals("int"));
		e = out[0].getExtendedEnv();
		assertEquals(e.getClassName(), "Rectangle");
		assertEquals(e.getPackageName(), "");
		e = e.getExtendedEnv();
		assertEquals(e.getClassName(), "Rectangle");
		assertEquals(e.getPackageName(), "");
		e = out[0].getExtendedEnv("x");
		assertEquals(e.getClassName(), "Shape");
		assertEquals(e.getPackageName(), "");


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
