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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The following class implements a connection to MongoDB.
 * It allows to store and retrieve {@link IndexData} objects.
 * TODO: extend the possibility to specify where MongoDB is.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MongoConnector {
	private static HashMap<String,MongoConnector> instances = new HashMap<String, MongoConnector>();

	MongoDatabase db;
	MongoCollection<Document> indexCollection;
	final Morphia morphia = new Morphia();
	Datastore datastore;

	/**
	 * Protected constructor. We want to give a database as singleton.
	 * @param db_name Name of the DB to use
	 */
	protected MongoConnector(String db_name) {
		MongoClient mongoClient = new MongoClient();
		db = mongoClient.getDatabase(db_name);
		indexCollection = db.getCollection("IndexData");
		datastore = morphia.createDatastore(mongoClient, db_name);
		MapperOptions options = new MapperOptions();
		options.setStoreEmpties(true);
		options.setStoreNulls(true);
		morphia.getMapper().setOptions(options);
		morphia.map(IndexData.class);
		datastore.ensureIndexes();
	}

	/**
	 * Get the instance of the DB with lazy initialization.
	 * @param db_name Name of the database to use
	 * @return The singleton of the selected DB.
	 */
	public static synchronized MongoConnector getInstance(String db_name) {
		if(instances.containsKey(db_name)){
			return instances.get(db_name);
		}
		MongoConnector m = new MongoConnector(db_name);
		instances.put(db_name, m);
		return m;
	}

	/**
	 * Check if a class is already in the DB.
	 * @param name			Name to search
	 * @param packageName	PackageName to search
	 * @return				True if it is already in the DB.
	 */
	public boolean existClassIndex(String name, String packageName){
		return getIndex(name,packageName).size() > 0;
	}

	/**
	 * Check if a class is already in the DB.
	 * @param c	{@link ASTClass} to search
	 * @return 	True if it is already in the DB.
	 */
	public boolean existClassIndex(ASTClass c){
		return existClassIndex(c.getName(), c.getPackageName());
	}

	/**
	 * Check if a class is already in the DB.
	 * @param i	{@link IndexData} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existClassIndex(IndexData i){
		return existClassIndex(i.getClassName(), i.getClassPackage());
	}

	/**
	 *
	 * @param indexStructureClass
	 */
	public void add(IndexData indexStructureClass){
		if(existClassIndex(indexStructureClass)){
			return;
		}
		datastore.save(indexStructureClass);
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexData> getIndex(String name, String packageName){
		return  datastore.createQuery(IndexData.class)
				.field("Name").equal(name)
				.field("classPackage").equal(packageName)
				.asList();
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param c	{@link ASTClass} to get
	 * @return	List of {@link IndexData} documents
	 */
	public List<IndexData> getIndex(ASTClass c){
		return  getIndex(c.getName(), c.getPackageName());
	}

	/**
	 * It queries the database to retrieve all the classes that are Threads.
	 * @return list of {@link IndexData} objects.
	 */
	public List<IndexData> getThreads(){
		Query<IndexData> q = datastore.createQuery(IndexData.class);
		q.or(
				q.criteria("extendedType").equal("Thread"),
				q.criteria("interfacesImplemented").contains("Runnable")
		);
		return q.asList();
	}


	public List<IndexData> getFromImport(String query){
		// create a regular expression which matches any string which includes "test"
		Pattern regexp = Pattern.compile(query);
		// use this regular expression to create a query
		Query<IndexData> q = datastore.createQuery(IndexData.class);//.filter("classPackage",regexp);
		if(query.endsWith("*")){
			q.field("classPackage").startsWith(query);
		} else {
			q.field("classPackage").equal(query);
		}
		return q.search(query).asList();
	}

	/**
	 * Getter of the DB structure.
	 * @return the database structure.
	 */
	public MongoDatabase getDb() {
		return db;
	}
}