import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestSyncBlocks {

	String file = TestSyncBlocks.class.getClassLoader().getResource("exprTypesSync/Thread_1.java").getPath();
	@Before
	public void setUp() throws Exception {

		String directory = file.substring(0, file.lastIndexOf("/")+1);


		MongoOptions.getInstance().setDbName("testSyncBlocks");
		MongoConnector.getInstance().ensureIndexes();

		IndexingProject indexing = new IndexingProject();
		indexing.indexProject(directory, true);
		indexing.indexSyncBlock(directory, false);
		
	}

	@Test
	public void TestSyncs() throws Exception {
		List<ASTClass> classes = JDTVisitor.parse(file);
		ASTClass c = classes.get(0);
		List<IndexSyncBlock> ret = MongoConnector.getInstance().getSyncBlockIndex(c);
		assertEquals(5, ret.size());
		IndexSyncBlock s;
		//1st
		s = ret.get(0);
		assertEquals("this", s.getExpr());
		assertEquals("test", s.getExprPkg());
		assertEquals("Thread_1", s.getExprType());
		//2nd
		s = ret.get(1);
		assertEquals("Thread_1.class", s.getExpr());
		assertEquals("test", s.getExprPkg());
		assertEquals("Thread_1", s.getExprType());
		//3rd
		s = ret.get(2);
		assertEquals("var", s.getExpr());
		assertEquals("test", s.getExprPkg());
		assertEquals("Thread_2", s.getExprType());
		//4th
		s = ret.get(3);
		assertEquals("var.minPrime", s.getExpr());
		assertEquals("java.lang", s.getExprPkg());
		assertEquals("ObjectTypes", s.getExprType());
		//5th
		s = ret.get(4);
		assertEquals("var.init(xyz)", s.getExpr());
		assertEquals("", s.getExprPkg());
		assertEquals("String", s.getExprType());
	}
}
