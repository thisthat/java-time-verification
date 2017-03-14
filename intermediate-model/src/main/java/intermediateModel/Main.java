package intermediateModel;

import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.converter.GenerateJSON;
import intermediateModelHelper.heuristic.definition.AnnotatedTypes;
import intermediateModelHelper.heuristic.definition.SetTimeout;
import intermediateModelHelper.heuristic.definition.TimeoutResources;
import intermediateModelHelper.indexing.IndexingProject;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import org.javatuples.Triplet;
import parser.UnparsableException;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	/*
	public static void main(String[] args) throws Exception {



		List<String> files = new ArrayList<>();
		//files.add( Main.class.getClassLoader().getResource("DiningPhilosopher.java").getFile() );
		files.add( "/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/com/aelitis/azureus/core/impl/AzureusCoreImpl.java" );
		//files.add( "/Users/giovanni/repository/java-xal/intermediate-model/src/main/resources/TestSocket.java" );
		//files.add( "/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/org/gudy/azureus2/core3/util/UrlUtils.java" );
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

				for(Triplet<String,IASTStm, Class> cc : cnst){
					System.out.println(cc.getValue0());
					System.out.println("\n\n");
				}

			}
		}

	}
	*/


	public static void main(String[] args) throws Exception {



		String name = args.length > 0 ? args[0] : "output";

		name = name.replace(".", "_");

		String base_path = args.length > 0 ? args[1] :"/Users/giovanni/repository/java-xal/project_eval/";

		String csvFile = name + ".csv";
		System.out.println(base_path + "\n" + csvFile);

		MongoOptions.getInstance().setDbName(name);

		FileWriter writer = new FileWriter(csvFile);
		writer.write("path;implicit_timeout;set_timeout;expired_resource\n");
		writer.flush();
		List<String> files = new ArrayList<>();
		IndexingProject index = new IndexingProject();
		{
			Iterator<File> f = index.getJavaFiles(base_path);
			while (f.hasNext()) {
				files.add(f.next().getAbsolutePath());
			}
		}
		System.out.println("#files " + files.size());
		int counter = 0;
		//files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){
			String f = files.get(i);
			List<ASTClass> lists = new ArrayList<>();
			try {
				lists = JDTVisitor.parse(f, base_path);
			} catch (UnparsableException e){
				counter++;
			}
			//List<ASTClass> lists = JDTVisitor.parse(f,"/Users/giovanni/repository/java-xal/project_eval/activemq/" );
			for(ASTClass c : lists){
				int implicit_timeout = 0;
				int set_timeout = 0;
				int resource_expired = 0;
				List<Triplet<String,IASTStm,Class>> cnst =  ApplyHeuristics.getConstraint(c);
				if(cnst.size() > 0){
					for(Triplet<String,IASTStm,Class> t : cnst){
						Class type = t.getValue2();
						if(type.equals(AnnotatedTypes.class)){
							implicit_timeout++;
						}
						if(type.equals(SetTimeout.class)){
							set_timeout++;
						}
						if(type.equals(TimeoutResources.class)){
							resource_expired++;
						}
					}
					String line = String.format("%s;%d;%d;%d\n", c.getPath(), implicit_timeout, set_timeout, resource_expired);
					//System.out.println(line);
					writer.write(line);
					writer.flush();
				}
			}
		}
		writer.flush();
		writer.close();
		Utils.send_email(String.format("Project %s {%s} ends.\r\n", name, base_path));
		System.out.println("#Skipped " + counter);
	}
}
