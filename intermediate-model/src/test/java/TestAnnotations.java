import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.creation.JDTVisitor;
import org.junit.Before;
import org.junit.Test;
import timeannotation.definition.Annotation;
import timeannotation.definition.ClockAnnotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestAnnotations {


	@Before
	public void setUp() throws Exception {
		MongoOptions.getInstance().setDbName("intermediate_model_" + this.getClass().getSimpleName());
		MongoConnector.getInstance().drop();
		MongoConnector.getInstance().ensureIndexes();
	}

	@Test
	public void clock() throws Exception {
		List<Annotation> ann = null;
		ClockAnnotation cl;

		//first method
		String f =  TestAnnotations.class.getClassLoader().getResource("annotations/Clock.java").getFile();
		//we have only one class
		ASTClass c = JDTVisitor.parse(f).get(0);

		//method run
		IASTMethod run = c.getFirstMethodByName("run");
		//ann = run.getStms().get(1).getAnnotations();
		assertEquals(ann.size(), 1);
		assertTrue(ann.get(0) instanceof ClockAnnotation);
		cl = (ClockAnnotation) ann.get(0);
		assertEquals(cl.getName(), "do_smth");

		//method init
		IASTMethod init = c.getFirstMethodByName("init");
		//first par with @clock
		//ann = init.getParameters().get(0).getAnnotations();
		assertEquals(ann.size(), 1);
		assertTrue(ann.get(0) instanceof ClockAnnotation);
		cl = (ClockAnnotation) ann.get(0);
		assertEquals(cl.getName(), "xtemp");
	}

}
