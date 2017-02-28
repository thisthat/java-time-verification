package intermediateModel;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.converter.GenerateJSON;

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
		files.add( "/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/com/aelitis/azureus/core/impl/AzureusCoreImpl.java" );

		//files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			String name = f.substring( f.lastIndexOf("/")+1, f.lastIndexOf("."));
			List<ASTClass> lists = JDTVisitor.parse(f,"/Users/giovanni/repository/java-xal/project_eval/vuze/src/main/java/" );

			for(ASTClass c : lists){
				//Create JSON
				GenerateJSON conv = new GenerateJSON();
				FileWriter write = new FileWriter(name + ".json");
				write.write(conv.convert(c));
				write.flush();
				write.close();
			}
		}

	}
}
