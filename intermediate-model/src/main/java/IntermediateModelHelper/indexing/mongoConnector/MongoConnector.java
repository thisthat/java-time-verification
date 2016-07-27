package IntermediateModelHelper.indexing.mongoConnector;

import IntermediateModelHelper.indexing.Indexing;
import IntermediateModelHelper.indexing.reducedstructure.IndexMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import intermediateModel.structure.ASTClass;
import org.bson.Document;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MongoConnector {
	private static final MongoConnector instance = new MongoConnector();


	MongoDatabase db;
	MongoCollection<Document> indexCollection;

	protected MongoConnector(String ip, String port){

	}

	protected MongoConnector() {
		MongoClient mongoClient = new MongoClient();
		db = mongoClient.getDatabase("test");
		indexCollection = db.getCollection("indexing");
	}

	public static MongoConnector getInstance() {
		return instance;
	}

	public boolean existClassIndex(String name, String packageName){
		FindIterable<Document> iterable = indexCollection.find(
				Filters.and(
						Filters.eq("name", name), Filters.eq("packageName", packageName)
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

	public boolean existClassIndex(ASTClass c){
		return existClassIndex(c.getName(), c.getPackageName());
	}

	public boolean existClassIndex(Indexing i){
		return existClassIndex(i.getClassName(), i.getClassPackage());
	}

	public void add(Indexing indexStructureClass){
		if(existClassIndex(indexStructureClass)){
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		// Convert object to JSON string
		try {
			String jsonInString = mapper.writeValueAsString(indexStructureClass);
			indexCollection.insertOne(Document.parse(jsonInString));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

}
