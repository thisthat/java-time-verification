import intermediateModel.interfaces.IASTMethod;
import intermediateModel.metrics.CountVars;
import intermediateModel.metrics.CyclomaticComplexity;
import intermediateModel.metrics.NumberOfStatements;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestMetrics {

	@Test
	public void TestCountNumberOfStatements() throws Exception {
		ClassLoader classLoader = TestMetrics.class.getClassLoader();
		String file = classLoader.getResource("metrics/Metrics.java").getFile();
		List<ASTClass> cs = JDTVisitor.parse(file, file.substring(0, file.lastIndexOf("/")));
		assertEquals(1, cs.size());
		ASTClass c = cs.get(0);
		IASTMethod m = c.getFirstMethodByName("Test");
		assertNotNull(m);
		assertEquals(55, NumberOfStatements.get(m));
	}

    @Test
    public void TestCountCyclomaticComplexity() throws Exception {
        ClassLoader classLoader = TestMetrics.class.getClassLoader();
        String file = classLoader.getResource("metrics/Metrics.java").getFile();
        List<ASTClass> cs = JDTVisitor.parse(file, file.substring(0, file.lastIndexOf("/")));
        assertEquals(1, cs.size());
        ASTClass c = cs.get(0);
        IASTMethod m = c.getFirstMethodByName("Test");
        assertNotNull(m);
        assertEquals(29, CyclomaticComplexity.get(m));
    }

    @Test
    public void TestCountVars() throws Exception {
        ClassLoader classLoader = TestMetrics.class.getClassLoader();
        String file = classLoader.getResource("metrics/Metrics.java").getFile();
        List<ASTClass> cs = JDTVisitor.parse(file, file.substring(0, file.lastIndexOf("/")));
        assertEquals(1, cs.size());
        ASTClass c = cs.get(0);
        IASTMethod m = c.getFirstMethodByName("Test");
        assertNotNull(m);
        assertEquals(13, CountVars.get(m, c));
    }

}
