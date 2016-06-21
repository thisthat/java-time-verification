package testing;


/* WALA
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
*/

import org.neo4j.driver.v1.*;
/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {


	public static void main(String[] args) {

	}

	/*
	public static void main(String[] args) {

		Driver driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "xal" ) );
		Session session = driver.session();

		session.run( "CREATE (a:ASTClass {name:'Arthur', title:'King'})" );

		StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = 'Arthur' RETURN a.name AS name, a.title AS title" );
		while ( result.hasNext() )
		{
			Record record = result.next();
			System.out.println( record.get( "title" ).asString() + " " + record.get("name").asString() );
		}

		session.close();
		driver.close();

	}



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
	*/

}
