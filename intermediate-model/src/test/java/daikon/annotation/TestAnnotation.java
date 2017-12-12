package daikon.annotation;

import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestAnnotation {




	@Test
	public void verifyAnnotation() {

		int first = 57;
		int second = 62;
		int third = 72;
		int fourth = 73;
		//first method
		String f =  TestAnnotation.class.getClassLoader().getResource("kafka-bug/WorkerCoordinator_issue.java").getFile();
		ASTClass c = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/"))).get(0);
		ApplyHeuristics.getConstraintV2(c);
		TimeVarCollector timeVarCollector = new TimeVarCollector();
		WatchingPoints variables = timeVarCollector.getTimeVariables(c);
		System.out.println(variables);
		assertEquals(4, variables.size());
		Set<Integer> keys = new HashSet<>();
		keys.add(first);
		keys.add(second);
		keys.add(third);
		keys.add(fourth);
		assertEquals(keys, variables.getLines());

		assertEquals(3, variables.getVarsByLine(first).size());
		assertEquals(3, variables.getVarsByLine(second).size());
		assertEquals(4, variables.getVarsByLine(third).size());
		assertEquals(3, variables.getVarsByLine(fourth).size());


	}


}
