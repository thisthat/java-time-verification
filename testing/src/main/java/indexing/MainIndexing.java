package indexing;

import IntermediateModelHelper.heuristic.*;
import IntermediateModelHelper.indexing.Indexing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.JDTVisitor;
import org.bson.Document;
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
public class MainIndexing {

	MongoDatabase db;
	MongoCollection<Document> coll;


	public static void main(String args[]) throws IOException, ParseErrorsException {
		new MainIndexing().run();
	}

	private void run() throws IOException, ParseErrorsException {
		MongoClient mongoClient = new MongoClient();
		db = mongoClient.getDatabase("test");
		coll = db.getCollection("indexing");


		List<String> files = new ArrayList<>();
		files.add( MainIndexing.class.getClassLoader().getResource("HeavyParsing.java").getFile() );
		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
			CompilationUnit ast = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(ast);
			ast.accept(v);


			for(ASTClass c : v.listOfClasses){
				Indexing indexing = new Indexing(c);


			}
		}
	}
}
