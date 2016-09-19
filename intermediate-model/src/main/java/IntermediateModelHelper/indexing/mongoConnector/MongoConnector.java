package IntermediateModelHelper.indexing.mongoConnector;

import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import IntermediateModelHelper.indexing.structure.IndexSyncCall;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import intermediateModel.structure.ASTClass;
import org.bson.BsonSerializationException;
import org.bson.Document;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The following class implements a connection to MongoDB.
 * It allows to store and retrieve {@link IndexData} objects.
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
	Map<String,List<IndexData>> cacheImport = new HashMap<>();
	Map<Pair<String,String>,List<IndexData>> cacheIndex = new HashMap<>();
	Map<Pair<String,String>,List<IndexSyncCall>> cacheSyncCall = new HashMap<>();
	Map<Sextet<String,String,String,List<String>,Integer,Integer>,List<IndexSyncBlock>> cacheSyncBlock = new HashMap<>();

	/**
	 * Field enumeration
	 */
	private final String __PACKAGE_NAME		= "classPackage";
	private final String __CLASS_NAME 		= "name";
	private final String __IMPORTS 			= "imports";
	private final String __FULL_NAME 		= "fullName";
	private final String __COLLECTION_NAME 	= "IndexData";
	private final String __METHOD_NAME 		= "methodName";
	private final String __SIGNATURE 		= "signature";
	private final String __START	 		= "start";
	private final String __END		 		= "end";

	/**
	 * Protected constructor. We want to give a database as singleton.
	 * @param db_name Name of the DB to use
	 */
	protected MongoConnector(String db_name, String ip, int port) {
		MongoClient mongoClient = new MongoClient(ip, port);
		db = mongoClient.getDatabase(db_name);
		indexCollection = db.getCollection(__COLLECTION_NAME);
		datastore = morphia.createDatastore(mongoClient, db_name);
		MapperOptions options = new MapperOptions();
		options.setStoreEmpties(true);
		options.setStoreNulls(true);
		morphia.getMapper().setOptions(options);
		morphia.mapPackage("IntermediateModelHelper.indexing.structure");
		datastore.ensureIndexes();
		datastore.ensureCaps();
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
		MongoOptions options = MongoOptions.getInstance();
		MongoConnector m = new MongoConnector(db_name, options.getIp(), options.getPort());
		instances.put(db_name, m);
		return m;
	}

	/**
	 * Get the instance of the default DB with lazy initialization.
	 * @return The singleton of the selected DB.
	 */
	public static synchronized MongoConnector getInstance() {
		MongoOptions options = MongoOptions.getInstance();
		String db_name = options.getDbName();
		if(instances.containsKey(db_name)){
			return instances.get(db_name);
		}
		MongoConnector m = new MongoConnector(db_name, options.getIp(), options.getPort());
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
	 * Check if a class is already in the DB in the collection of Synchronized calls.
	 * @param c	{@link ASTClass} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncCallIndex(ASTClass c) {
		return existSyncCallIndex(c.getName(),c.getPackageName());
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized calls.
	 * @param i	{@link IndexSyncCall} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncCallIndex(IndexSyncCall i){
		return existSyncCallIndex(i.getClassName(), i.getPackageName());
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized calls.
	 * @param name			Name of the class to search
	 * @param packageName	Package of the class to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncCallIndex(String name, String packageName){
		return getSyncIndex(name,packageName).size() > 0;
	}

	/**
	 * Check if a class is already in the DB in the collection of Synchronized blocks.
	 * @param c	{@link ASTClass} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncBlockIndex(ASTClass c) {
		return getSyncBlockIndexClass(c.getName(),c.getPackageName()).size() > 0;
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized blocks.
	 * @param i	{@link IndexSyncBlock} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncBlockIndex(IndexSyncBlock i){
		return existSyncBlockIndex(i.getName(), i.getPackageName(), i.getMethodName(), i.getSignature(), i.getStart(), i.getEnd());
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized blocks.
	 * @param name			Name of the class to search
	 * @param packageName	Package of the class to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncBlockIndex(String name, String packageName, String methodName, List<String> signature, int start, int end){
		return getSyncBlockIndex(name,packageName, methodName, signature, start, end).size() > 0;
	}

	/**
	 * Insert a indexed class in the database iff. it is not already present
	 * @param indexStructureClass	Indexed structure of a class to insert
	 */
	public void add(IndexData indexStructureClass){
		if(existClassIndex(indexStructureClass)){
			return;
		}
		//System.out.println("Saving " + indexStructureClass.getClassName());
		try {
			datastore.save(indexStructureClass);
		} catch (BsonSerializationException e){
			System.err.println("File too big, cannot handle it!");
		}
	}

	public void add(IndexSyncCall indexSyncCall){
		if(existSyncCallIndex(indexSyncCall)){
			return;
		}
		try {
			datastore.save(indexSyncCall);
		} catch (BsonSerializationException e){
			System.err.println("File too big, cannot handle it!");
		}
	}

	public void add(IndexSyncBlock indexSyncBlock) {
		if(existSyncBlockIndex(indexSyncBlock)){
			return;
		}
		try {
			datastore.save(indexSyncBlock);
		} catch (BsonSerializationException e){
			System.err.println("File too big, cannot handle it!");
		}
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexData> getIndex(String name, String packageName){
		Pair<String,String> p = new Pair<>(name,packageName);
		if(cacheIndex.containsKey(p)){
			return cacheIndex.get(p);
		}
		List<IndexData> out =  datastore.createQuery(IndexData.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.asList();
		if(out.size() > 0) {
			cacheIndex.put(p, out);
		}
		return out;
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param c	{@link ASTClass} to get
	 * @return	List of {@link IndexData} documents
	 */
	public List<IndexData> getIndex(ASTClass c){
		return  getIndex(c.getName(), c.getPackageName());
	}


	public List<IndexSyncCall> getSyncIndex(ASTClass c) {
		return getSyncIndex(c.getName(), c.getPackageName());
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexSyncCall> getSyncIndex(String name, String packageName){
		Pair<String,String> p = new Pair<>(name,packageName);
		if(cacheSyncCall.containsKey(p)){
			return cacheSyncCall.get(p);
		}
		List<IndexSyncCall> out =  datastore.createQuery(IndexSyncCall.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.asList();
		if(out.size() > 0) {
			cacheSyncCall.put(p, out);
		}
		return out;
	}

	public List<IndexSyncBlock> getSyncBlockIndex(ASTClass c) {
		return getSyncBlockIndexClass(c.getName(), c.getPackageName());
	}

	/**
	 * Retrieve the list of sync block of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexSyncBlock> getSyncBlockIndex(String name, String packageName, String methodName, List<String> signature, int start, int end){
		Sextet<String,String,String,List<String>,Integer,Integer> p = new Sextet<>(name,packageName, methodName, signature, start, end);
		if(cacheSyncBlock.containsKey(p)){
			return cacheSyncBlock.get(p);
		}
		List<IndexSyncBlock> out =  datastore.createQuery(IndexSyncBlock.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.field(__METHOD_NAME).equal(methodName)
				.field(__SIGNATURE).equal(signature)
				.field(__START).equal(start)
				.field(__END).equal(end)
				.asList();
		if(out.size() > 0) {
			cacheSyncBlock.put(p, out);
		}
		return out;
	}

	/**
	 * Retrieve the list of sync block of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexSyncBlock> getSyncBlockIndexClass(String name, String packageName){
		List<IndexSyncBlock> out =  datastore.createQuery(IndexSyncBlock.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.asList();
		return out;
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

	/**
	 * Get the list of classes that belong to an import statement
	 * @param query	package name of the import
	 * @return	List of {@link IndexData} classes
	 */
	public List<IndexData> getFromImport(String query, boolean forceCache){
		if(cacheImport.containsKey(query)){
			return cacheImport.get(query);
		}
		Query<IndexData> q = datastore.createQuery(IndexData.class);//.filter("classPackage",regexp);
		if(query.endsWith("*")){
			q.field(__FULL_NAME).startsWith(query);
		} else {
			q.field(__FULL_NAME).equal(query);
		}
		List<IndexData> out = q.search(query).asList();
		if(out.size() > 0) {
			cacheImport.put(query, out);
		} else { // if(forceCache){
			cacheImport.put(query, new ArrayList<>());
		}
		return out;
	}


	public List<IndexData> getClassesThatImports(String _package, String name){
		Query<IndexData> q = datastore.createQuery(IndexData.class);
		q.or(
			q.criteria(__IMPORTS).contains(_package + ".*"),
			q.criteria(__IMPORTS).contains(_package + "." + name)
		);
		return q.asList();
	}

	public void ensureIndexes(){
		datastore.ensureIndexes();
	}

	/**
	 * Getter of the DB structure.
	 * @return the database structure.
	 */
	public MongoDatabase getDb() {
		return db;
	}

	/**
	 * Getter of the DB structure.
	 * @return the database structure.
	 */
	public Datastore getDatastore() {
		return datastore;
	}

	/**
	 * Delete a class from the DB
	 * @param c	Class to remove
	 */
	public void delete(ASTClass c) {
		Query<IndexData> q = datastore.createQuery(IndexData.class)
				.field(__CLASS_NAME).equal(c.getName())
				.field(__PACKAGE_NAME).equal(c.getPackageName());
		datastore.delete(q);
		Pair<String,String> p = new Pair<>(c.getName(),c.getPackageName());
		cacheIndex.remove(p);
	}

	/**
	 * Delete the current database
	 */
	public void drop(){
		this.db.drop();
		this.cacheIndex = new HashMap<>();
		this.cacheImport = new HashMap<>();
		this.cacheSyncBlock = new HashMap<>();
		this.cacheSyncCall = new HashMap<>();
	}


	public void deleteSyncCalls(ASTClass c) {
		Query<IndexSyncCall> q = datastore.createQuery(IndexSyncCall.class)
				.field(__CLASS_NAME).equal(c.getName())
				.field(__PACKAGE_NAME).equal(c.getPackageName());
		datastore.delete(q);
		Pair<String,String> p = new Pair<>(c.getName(),c.getPackageName());
		cacheSyncCall.remove(p);
	}

	public void deleteSyncBlock(ASTClass c) {
		Query<IndexSyncBlock> q = datastore.createQuery(IndexSyncBlock.class)
				.field(__CLASS_NAME).equal(c.getName())
				.field(__PACKAGE_NAME).equal(c.getPackageName());
		datastore.delete(q);
		Pair<String,String> p = new Pair<>(c.getName(),c.getPackageName());
		cacheSyncBlock.remove(p);
	}
}