package IntermediateModelHelper.indexing.mongoConnector;

import IntermediateModelHelper.indexing.structure.IndexData;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import intermediateModel.structure.ASTClass;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MongoConnector {
	private static HashMap<String,MongoConnector> instances = new HashMap<String, MongoConnector>();

	MongoDatabase db;
	MongoCollection<Document> indexCollection;
	final Morphia morphia = new Morphia();
	Datastore datastore;


	protected MongoConnector(String db_name) {
		MongoClient mongoClient = new MongoClient();
		db = mongoClient.getDatabase(db_name);
		indexCollection = db.getCollection("IndexData");
		datastore = morphia.createDatastore(mongoClient, db_name);
		MapperOptions options = new MapperOptions();
		options.setStoreEmpties(true);
		options.setStoreNulls(true);
		morphia.getMapper().setOptions(options);
		morphia.mapPackage("intermediateModel.structure");
		datastore.ensureIndexes();
	}

	public static synchronized MongoConnector getInstance(String db_name) {
		if(instances.containsKey(db_name)){
			return instances.get(db_name);
		}
		MongoConnector m = new MongoConnector(db_name);
		instances.put(db_name, m);
		return m;
	}

	public boolean existClassIndex(String name, String packageName){
		return getIndex(name,packageName).size() > 0;
	}

	public boolean existClassIndex(ASTClass c){
		return existClassIndex(c.getName(), c.getPackageName());
	}

	public boolean existClassIndex(IndexData i){
		return existClassIndex(i.getClassName(), i.getClassPackage());
	}

	public void add(IndexData indexStructureClass){
		if(existClassIndex(indexStructureClass)){
			return;
		}
		datastore.save(indexStructureClass);
	}

	public List<IndexData> getIndex(String name, String packageName){
		return  datastore.createQuery(IndexData.class)
				.field("className").equal(name)
				.field("classPackage").equal(packageName)
				.asList();
	}
	public List<IndexData> getIndex(ASTClass c){
		return  getIndex(c.getName(), c.getPackageName());
	}

	public List<IndexData> getThreads(){
		Query<IndexData> q = datastore.createQuery(IndexData.class);
		q.or(
				q.criteria("extendedType").equal("Thread"),
				q.criteria("interfacesImplemented").contains("Runnable")
		);
		return q.asList();
	}

	public MongoDatabase getDb() {
		return db;
	}
}