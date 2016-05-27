package testing;


import IntermediateModel.structure.ASTClass;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.ArrayList;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args){
		//Driver driver =  GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "xal" ) );
		//Session session = driver.session();
		//session.run( "CREATE (a:Person {name:'Arthur', title:'King'})" );
		Configuration configuration = new Configuration();
		configuration.driverConfiguration()
				.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
				.setURI("http://neo4j:xal@localhost:7474");
		SessionFactory sessionFactory = new SessionFactory(configuration,"test.domain");
		Session session = sessionFactory.openSession();

		//StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = 'Arthur' RETURN a.name AS name, a.title AS title" );
		//while ( result.hasNext() )
		//{
		//	Record record = result.next();
		//	System.out.println( record.get( "title" ).asString() + " " + record.get("name").asString() );
		//}
		Token t = new CommonToken(0);
		ASTClass c = new ASTClass(t,t, "at.aau.service.jobs", "ExportChangesJob", ASTClass.Visibility.PUBLIC, "Thread", new ArrayList<>());
		session.save(c);
		session.load(c)
		//session.close();
		//driver.close();
	}
}
