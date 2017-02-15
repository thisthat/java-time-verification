package intermediateModel;

import IntermediateModelHelper.converter.GenerateJSON;
import IntermediateModelHelper.converter.GenerateXAL;
import IntermediateModelHelper.converter.IConverterIM;
import IntermediateModelHelper.heuristic.definition.*;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import timeannotation.parser.Java2AST;

import java.io.FileWriter;
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
		files.add( Main.class.getClassLoader().getResource("DiningPhilosopher.java").getFile() );
		files.add( Main.class.getClassLoader().getResource("Philosopher.java").getFile() );

		//files.add(args[0]);
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			String name = f.substring( f.lastIndexOf("/")+1, f.lastIndexOf("."));
			List<ASTClass> lists = JDTVisitor.parse(f);

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
