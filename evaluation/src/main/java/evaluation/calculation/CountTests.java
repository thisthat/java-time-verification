package evaluation.calculation;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTMethod;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CountTests {

	public static void main(String... args){
		String[] projects = {"activemq","airavata", "jetty", "vuze", "wildfly-core"};
		for(String p : projects){
			System.out.println(p);
			compute(p);
			System.out.println();
		}
	}

	private static void compute(String project){
		String base_path = "/Users/giovanni/repository/sources/" + project;
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		int n_test = 0;
		int n_meth = 0;
		int n_smet = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(filename.contains("/test")){
				n_test++;
			} else {
				n_file++;
				Java2AST a = null;
				try {
					a = new Java2AST(filename, true);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				CompilationUnit result = a.getContextJDT();
				JDTVisitor v = new JDTVisitor(result, filename);
				result.accept(v);
				//pp filename
				for(ASTClass c : v.listOfClasses){
					for(IASTMethod m : c.getMethods()){
						n_meth++;
						if(m instanceof ASTMethod && ((ASTMethod) m).isSyncronized()){
							n_smet++;
						}
					}
				}
			}
		}
		System.out.println("Classes: " + n_file);
		System.out.println("Tests  : " + n_test);
		System.out.println("Tot    : " + (n_test + n_file));
		System.out.println("Methods: " + n_meth);
		System.out.println("S.Metho: " + n_smet);
	}


}
