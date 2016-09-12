package testing;/* WALA
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


import org.neo4j.driver.v1.*;
*/


import IntermediateModelHelper.heuristic.definition.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MainTesting {


	public static void main(String[] args) throws IOException, ParseErrorsException {
		new MainTesting().run();
	}

	public void run() throws IOException, ParseErrorsException {
		List<String> files = new ArrayList<>();
		files.add( MainTesting.class.getResource("JavaTimerExampleTask.java").getFile() );
		//files.add( Main.class.getResource("FailoverTimeoutTest.java").getFile() );
		//files.add( Main.class.getResource("MCGroupImpl.java").getFile() );
		//files.add( Main.class.getResource("UpnPImpl.java").getFile() );

		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
			CompilationUnit ast = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(ast,f);
			ast.accept(v);


			ApplyHeuristics ah = new ApplyHeuristics();
			ah.subscribe(ThreadTime.class);
			ah.subscribe(SocketTimeout.class);
			ah.subscribe(TimeoutResources.class);
			ah.subscribe(TimerType.class);
			ah.subscribe(AnnotatedTypes.class);

			for(ASTClass c : v.listOfClasses){

				ObjectMapper mapper = new ObjectMapper();
				// Convert object to JSON string
				String jsonInString = mapper.writeValueAsString(c);
				System.out.println(jsonInString);

				mapper.writerWithDefaultPrettyPrinter().writeValue(new File("test.json"), c);

				ah.analyze(c);
				String s = Arrays.toString( ah.getTimeConstraint().toArray() );
				System.err.println("[" + f + "]");
				System.err.println(s);
				System.err.println("__________");
			}
		}
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
