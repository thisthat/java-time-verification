package intermediateModel;

import IntermediateModelHelper.converter.GenerateXAL;
import IntermediateModelHelper.heuristic.definition.*;
import IntermediateModelHelper.indexing.IndexingFile;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args) throws Exception {



		List<String> files = new ArrayList<>();
		//files.add( Main.class.getClassLoader().getResource("AttributeTimeRelated.java").getFile() );
		//files.add( Main.class.getClassLoader().getResource("ExportChangesJob.java").getFile() );
		//files.add( Main.class.getClassLoader().getResource("SmallTest.java").getFile() );
		files.add( Main.class.getClassLoader().getResource("Test.java").getFile() );
		//files.add( Main.class.getClassLoader().getResource("JavaTimerExampleTask.java").getFile() );
		//files.add( Main.class.getClassLoader().getResource("FailoverTimeoutTest.java").getFile() );
		//files.add( Main.class.getClassLoader().getResource("MCGroupImpl.java").getFile() );
		//files.add( "/Users/giovanni/repository/java-xal/evaluation-vuze/src/main/resources/top5package/com/aelitis/azureus/core/networkmanager/impl/tcp/SelectorGuard.java" );

		//files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			Java2AST a = new Java2AST(f, true);
			CompilationUnit ast = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(ast, f);
			ast.accept(v);


			ApplyHeuristics ah = new ApplyHeuristics();
			ah.subscribe(ThreadTime.class);
			ah.subscribe(SocketTimeout.class);
			ah.subscribe(TimeoutResources.class);
			ah.subscribe(TimerType.class);
			ah.subscribe(AnnotatedTypes.class);

			for(ASTClass c : v.listOfClasses){

				IndexingFile index = new IndexingFile();
				index.index(c);

				ah.analyze(c);
				String s = Arrays.toString( ah.getTimeConstraint().toArray() );
				System.err.println("[" + f + " :: " + c.getName() + "]");
				System.err.println(s);
				System.err.println("__________");

				//Create XAL
				GenerateXAL g = new GenerateXAL(c);
				g.getXalDocument().toFile("GenerateXALCFG");
			}
		}

	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
