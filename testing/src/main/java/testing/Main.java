package testing;


import java.io.File;
import java.io.IOException;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args) throws IOException, ClassHierarchyException {

		File exFile=new FileProvider().getFile("Java60RegressionExclusions.txt");
		System.out.println(exFile.getAbsolutePath());
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("test1.jar", exFile);

		IClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> entryPoints = Util.makeMainEntrypoints(scope,cha); //get method that have main


		AnalysisOptions options = new AnalysisOptions();
		options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());

		// Create an object which caches IRs and related information, reconstructing them lazily on demand.
		AnalysisCache cache = new AnalysisCache();

		for (IClass c : cha) {
			if (!scope.isApplicationLoader(c.getClassLoader())) continue;

			String cname = c.getName().toString();
			System.out.println("Class:" + cname);
			for (IMethod m : c.getAllMethods()) {
				String mname = m.getName().toString();
				System.out.println("  method:" + mname);
			}

			System.out.println();
		}
	}


}
