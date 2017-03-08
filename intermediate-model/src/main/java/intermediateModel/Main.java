package intermediateModel;

import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.converter.GenerateJSON;
import org.javatuples.Triplet;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args) throws Exception {



		List<String> files = new ArrayList<>();
		//files.add( Main.class.getClassLoader().getResource("DiningPhilosopher.java").getFile() );
		//files.add( "/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/com/aelitis/azureus/core/impl/AzureusCoreImpl.java" );
		files.add( "/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/org/gudy/azureus2/core3/util/UrlUtils.java" );
		//files.add( "/Users/giovanni/repository/java-xal/project_eval/activemq/activemq-client/src/main/java/org/apache/activemq/ActiveMQConnection.java" );

		//files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			String name = f.substring( f.lastIndexOf("/")+1, f.lastIndexOf("."));
			List<ASTClass> lists = JDTVisitor.parse(f,"/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/" );
			//List<ASTClass> lists = JDTVisitor.parse(f,"/Users/giovanni/repository/java-xal/project_eval/activemq/" );

			for(ASTClass c : lists){
				//Create JSON
				List<Triplet<String,IASTStm,Class>> cnst =  ApplyHeuristics.getConstraint(c);
				System.out.println("Class " + c.getName() + " : " + cnst.size());
				//for(Triplet<String,IASTStm, Class> cc : cnst){
				//	System.out.println(cc);
				//}

			}
		}

	}
}
