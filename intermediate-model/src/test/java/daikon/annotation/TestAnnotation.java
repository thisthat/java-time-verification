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



	private static String toString(Map map) {
		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = iter.next();
			sb.append(entry.getKey());
			sb.append('=').append('"');
			sb.append(entry.getValue());
			sb.append('"');
			if (iter.hasNext()) {
				sb.append(',').append(' ');
			}
			sb.append("\n");
		}
		return sb.toString();

	}



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
		Map<Integer, Set<IASTVar>> variables = timeVarCollector.getTimeVariables(c);
		System.out.println(toString(variables));
		assertEquals(4, variables.size());
		Set<Integer> keys = new HashSet<>();
		keys.add(first);
		keys.add(second);
		keys.add(third);
		keys.add(fourth);
		assertEquals(keys, variables.keySet());

		assertEquals(3, variables.get(first).size());
		assertEquals(3, variables.get(second).size());
		assertEquals(4, variables.get(third).size());
		assertEquals(3, variables.get(fourth).size());


	}


}
