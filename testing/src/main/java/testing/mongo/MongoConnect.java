package testing.mongo;

import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.structure.IndexData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MongoConnect {

	MongoDatabase db;
	MongoCollection<Document> coll;

	// db.testCollection.drop() -> delete collection
	public static void main(String[] args) throws IOException, ParseErrorsException {

		final Morphia morphia = new Morphia();
		MapperOptions options = new MapperOptions();
		options.setStoreEmpties(true);
		options.setStoreNulls(true);
		morphia.getMapper().setOptions(options);
		morphia.mapPackage("intermediateModel.structure");
		final Datastore datastore = morphia.createDatastore(new MongoClient(), "morphia_example");
		datastore.ensureIndexes();

		List<String> files = new ArrayList<>();
		files.add( MongoConnect.class.getClassLoader().getResource("JavaTimerExampleTask.java").getFile() );
		//files.add( Main.class.getResource("FailoverTimeoutTest.java").getFile() );
		//files.add( Main.class.getResource("MCGroupImpl.java").getFile() );
		//files.add( Main.class.getResource("UpnPImpl.java").getFile() );

		for(int i = 0; i < files.size(); i ++){

			String f = files.get(i);
			Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
			CompilationUnit ast = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(ast,f);
			ast.accept(v);

			for(ASTClass c : v.listOfClasses){
				IndexingFile ii = new IndexingFile();
				datastore.save(ii.index(c));
			}
		}

		final Query<IndexData> query = datastore.createQuery(IndexData.class);
		final List<IndexData> employees = query.asList();
		System.out.println(Arrays.toString(employees.toArray()));
	}


	public void run(String base_path) throws IOException, ParseErrorsException {
		//db
		MongoClient mongoClient = new MongoClient();
		db = mongoClient.getDatabase("test");
		coll = db.getCollection("testCollection");

		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();

		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			Java2AST a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			for(ASTClass c : v.listOfClasses){
				ObjectMapper mapper = new ObjectMapper();
				// Convert object to JSON string
				String jsonInString = mapper.writeValueAsString(c);
				if(exists(c)){
					coll.insertOne(Document.parse(jsonInString));
				}
			}

		}

	}

	private boolean exists(ASTClass c){
		FindIterable<Document> iterable = db.getCollection("testCollection").find(
				Filters.and(
						Filters.eq("name", c.getName()), Filters.eq("packageName",c.getPackageName())
				)
		).limit(1);
		final int[] howManyFind = {0};
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(Document document) {
				howManyFind[0]++;
			}
		});
		return howManyFind[0] > 0;
	}
}
