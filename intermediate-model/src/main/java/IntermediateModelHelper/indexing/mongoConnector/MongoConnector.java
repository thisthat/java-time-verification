package IntermediateModelHelper.indexing.mongoConnector;

import IntermediateModelHelper.indexing.structure.DBStatus;
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
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.sql.Timestamp;
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
	String dbName;
	DBStatus dbStatus;
	MongoCollection<Document> indexCollection;
	final Morphia morphia = new Morphia();
	Datastore datastore;
	Map<String,List<IndexData>> cacheImport = new HashMap<>();
	Map<Pair<String,String>,List<IndexData>> cacheIndex = new HashMap<>();
	Map<Pair<String,String>,List<IndexData>> cacheInterface = new HashMap<>();
	Map<Pair<String,String>,List<IndexData>> cacheExtended = new HashMap<>();
	Map<Quartet<String,String, String, List<String>>,List<IndexSyncCall>> cacheSyncCall = new HashMap<>();
	Map<Pair<String,String>,List<IndexSyncCall>> cacheSyncCallClass = new HashMap<>();
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
	private final String __IS_INTERFACE		= "isInterface";
	private final String __EXTENDED			= "extendedType";
	private final String __IMPLEMENTS		= "interfacesImplemented";
	private final String __SIGNATURE 		= "signature";
	private final String __SIGNATURE_SYNC 	= "methodSignature";
	private final String __START	 		= "start";
	private final String __END		 		= "end";

	/**
	 * Protected constructor. We want to give a database as singleton.
	 * @param db_name Name of the DB to use
	 */
	protected MongoConnector(String db_name, String ip, int port) {
		MongoClient mongoClient = new MongoClient(ip, port);
		db = mongoClient.getDatabase(db_name);
		dbName = db_name;
		indexCollection = db.getCollection(__COLLECTION_NAME);
		datastore = morphia.createDatastore(mongoClient, db_name);
		MapperOptions options = new MapperOptions();
		options.setStoreEmpties(true);
		options.setStoreNulls(true);
		morphia.getMapper().setOptions(options);
		morphia.mapPackage("IntermediateModelHelper.indexing.structure");
		datastore.ensureIndexes();
		datastore.ensureCaps();

		Query<DBStatus> q = datastore.createQuery(DBStatus.class);
		q.field("dbName").equal(dbName);
		try {
			List<DBStatus> r = q.asList();
			if (!(r.size() > 0)) {
				dbStatus = new DBStatus(dbName, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false);
				datastore.save(dbStatus);
			} else {
				dbStatus = r.get(0);
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}

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
	public boolean existSyncCallIndexClass(ASTClass c) {
		return getSyncCallIndexClass(c.getName(),c.getPackageName()).size() > 0;
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized calls.
	 * @param i	{@link IndexSyncCall} to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncCallIndex(IndexSyncCall i){
		return datastore.createQuery(IndexSyncCall.class)
				.field("line").equal(i.getLine())
				.field("path").equal(i.getPath())
				.field("classPackage").equal(i.getClassPackage())
				.field("name").equal(i.getClassName())
				.field("methodName").equal(i.getMethodName())
				.field("methodSignature").equal(i.getMethodSignature())
				.field("_inClassPackage").equal(i.get_inClassPackage())
				.field("_inClassName").equal(i.get_inClassName())
				.field("_inMethodName").equal(i.get_inMethodName())
				.field("_signatureInMethod").equal(i.get_signatureInMethod())
				.asList().size() > 0;
		//return false;
	}
	/**
	 * Check if a class is already in the DB in the collection of Synchronized calls.
	 * @param name			Name of the class to search
	 * @param packageName	Package of the class to search
	 * @return	True if it is already in the DB.
	 */
	public boolean existSyncCallIndex(String name, String packageName, String methodName, List<String> signature){
		return getSyncCallIndex(name,packageName, methodName, signature).size() > 0;
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
		} finally {
			Pair<String,String> p = new Pair<>(indexStructureClass.getClassName(),indexStructureClass.getClassPackage());
			cacheIndex.remove(p);
			cacheInterface.remove(p);
			String extType = indexStructureClass.getExtendedType();
			boolean f = false;
			for(String imp : indexStructureClass.getImports()){
				if(imp.endsWith(extType)){
					Pair<String,String> pExt = new Pair<>(imp, extType);
					cacheExtended.remove(pExt);
					f = true;
				}
			}
			if(!f) { //if we cannot find in the import, should be in the same pkg
				Pair<String,String> pExt = new Pair<>(indexStructureClass.getClassPackage(), extType);
				cacheExtended.remove(pExt);
			}
		}
	}

	public void add(IndexSyncCall indexSyncCall){
		if(existSyncCallIndex(indexSyncCall)){
			return;
		}
		try {
			datastore.save(indexSyncCall);
			Pair<String,String> p = new Pair<>(indexSyncCall.getClassName(),indexSyncCall.getPackageName());
			cacheSyncCallClass.remove(p);
			Quartet<String,String, String, List<String>> ps = new Quartet<>(indexSyncCall.getClassName(),indexSyncCall.getPackageName(), indexSyncCall.getMethodName(), indexSyncCall.getMethodSignature());
			cacheSyncCall.remove(ps);

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
			Sextet<String,String,String,List<String>,Integer,Integer> p = new Sextet<>(
					indexSyncBlock.getName(),
					indexSyncBlock.getPackageName(),
					indexSyncBlock.getMethodName(),
					indexSyncBlock.getSignature(),
					indexSyncBlock.getStart(),
					indexSyncBlock.getEnd()
			);
			cacheSyncBlock.remove(p);
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
		return getIndex(name, packageName, false);
	}

	/**
	 * Retrieve the list of classes from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexData> getIndex(String name, String packageName, boolean ignoreCache){
		Pair<String,String> p = new Pair<>(name,packageName);
		if(!ignoreCache && cacheIndex.containsKey(p)){
			return cacheIndex.get(p);
		}
		List<IndexData> out =  datastore.createQuery(IndexData.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.asList();
		//if(out.size() > 0) {
		cacheIndex.put(p, out);
		//}
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


	public List<IndexSyncCall> getSyncCallIndexClass(ASTClass c) {
		return getSyncCallIndexClass(c.getName(), c.getPackageName());
	}

	/**
	 * Retrieve the list of Sync Call from the database of a class.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexSyncCall> getSyncCallIndexClass(String name, String packageName){
		Pair<String,String> p = new Pair<>(name,packageName);
		if(cacheSyncCallClass.containsKey(p)){
			return cacheSyncCallClass.get(p);
		}
		List<IndexSyncCall> out =  datastore.createQuery(IndexSyncCall.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.asList();
		if(out.size() > 0) {
			cacheSyncCallClass.put(p, out);
		}
		return out;
	}

	/**
	 * Retrieve the list of Sync Call from the database.
	 * @param name			Name of the class to get
	 * @param packageName	PackageName of the class to get
	 * @param methodName    Method name to get
	 * @param methodSignature Signature of the method to search
	 * @return				List of {@link IndexData} documents
	 */
	public List<IndexSyncCall> getSyncCallIndex(String name, String packageName, String methodName, List<String> methodSignature){
		Quartet<String,String, String, List<String>> p = new Quartet<>(name,packageName, methodName, methodSignature);
		if(cacheSyncCall.containsKey(p)){
			return cacheSyncCall.get(p);
		}
		List<IndexSyncCall> out =  datastore.createQuery(IndexSyncCall.class)
				.field(__CLASS_NAME).equal(name)
				.field(__PACKAGE_NAME).equal(packageName)
				.field(__METHOD_NAME).equal(methodName)
				.field(__SIGNATURE_SYNC).equal(methodSignature)
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
	public List<IndexData> getFromImport(String query){
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
			q.criteria(__IMPORTS).equal(_package + ".*"),
			q.criteria(__IMPORTS).equal(_package + "." + name)
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

	public List<IndexData> resolveClassImplementingInterface(String interfaceName, String packageName){
		Pair<String,String> p = new Pair<>(interfaceName, packageName);
		if(cacheInterface.containsKey(p)){
			return cacheInterface.get(p);
		}
		List<IndexData> output = new ArrayList<>();
		Query<IndexData> q;
		List<IndexData> tmp;
		if(cacheIndex.containsKey(p)){
			tmp = cacheIndex.get(p);
		} else {
			q = datastore.createQuery(IndexData.class)
					.field(__CLASS_NAME).equal(interfaceName)
					.field(__PACKAGE_NAME).equal(packageName)
					.field(__IS_INTERFACE).equal(true);
			tmp = q.asList();
		}
		output.addAll(tmp);
		//first collect all the interfaces that extends the current one
		q = datastore.createQuery(IndexData.class)
				.field(__IS_INTERFACE).equal(true)
				.field(__IMPLEMENTS).contains(interfaceName);
		q.or(
				q.or(
						q.criteria(__IMPORTS).equal(packageName + ".*"),
						q.criteria(__IMPORTS).equal(packageName + "." + interfaceName)
				),
				q.criteria(__PACKAGE_NAME).equal(packageName)
		);
		tmp = q.asList();
		output.addAll(tmp);
		tmp.clear();
		//for each of it collect the class that implements it
		for(IndexData intf : output){
			q = datastore.createQuery(IndexData.class)
					.field(__IS_INTERFACE).equal(false)
					.field(__IMPLEMENTS).contains("(.?)" + intf.getName());
			if(Character.isUpperCase(intf.getFullclassPackage().substring(intf.getFullclassPackage().lastIndexOf(".")+1).charAt(0))){
				q.or(
						q.criteria(__IMPORTS).contains(intf.getFullclassPackage() + ".*"),
						q.criteria(__IMPORTS).contains(intf.getFullclassPackage() + "." + intf.getName()),
						q.criteria(__PACKAGE_NAME).equal(intf.getClassPackage())
				);
			} else {
				q.or(
						q.criteria(__IMPORTS).equal(intf.getClassPackage() + ".*"),
						q.criteria(__IMPORTS).equal(intf.getClassPackage() + "." + intf.getName()),
						q.criteria(__PACKAGE_NAME).equal(intf.getClassPackage())
				);
			}

			tmp.addAll( q.asList() );
		}
		output.addAll(tmp);
		//for each result collect the extensions of them until no one extended it anymore
		tmp.clear();
		int numNewInsert = output.size();
		List<IndexData> tmp_output = new ArrayList<>(output);
		while(numNewInsert != 0) {
			tmp.clear();
			for (IndexData data : tmp_output) {
				if (data.isInterface()) continue;
				tmp.addAll( getExtensionOfClass(data.getClassPackage(), data.getName()) );
			}
			numNewInsert = 0;
			tmp_output.clear();
			for(IndexData elm : tmp){
				if(!output.contains(elm)){
					numNewInsert++;
					output.add(elm);
					tmp_output.add(elm);
				}
			}
		}
		cacheInterface.put(p, output);
		return output;
	}

	public List<IndexData> getExtensionOfClass(String pkg, String name){
		Pair<String,String> p = new Pair<>(pkg, name);
		if(cacheExtended.containsKey(p)){
			return cacheExtended.get(p);
		}
		Query<IndexData> q;
		q = datastore.createQuery(IndexData.class)
				.field(__IS_INTERFACE).equal(false)
				.field(__EXTENDED).equal(name);
		q.or(
				q.criteria(__IMPORTS).equal(pkg + ".*"),
				q.criteria(__IMPORTS).equal(pkg + "." + name),
				q.criteria(__PACKAGE_NAME).equal(pkg)
		);
		List<IndexData> out =  q.asList();
		cacheExtended.put(p, out);
		return out;
	}

	public void setIndexStart(){
		try {
			this.dbStatus.setLastEdit(new Timestamp(System.currentTimeMillis()));
			this.dbStatus.setIndexed(false);
			this.datastore.save(dbStatus);
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}

	public void setIndexFinish(){
		try {
			this.dbStatus.setLastEdit(new Timestamp(System.currentTimeMillis()));
			this.dbStatus.setIndexed(true);
			this.datastore.save(dbStatus);
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}

	public boolean getIndexStatus(){
		Query<DBStatus> q = datastore.createQuery(DBStatus.class);
		q.field("dbName").equal(dbName);
		try {
			List<DBStatus> r = q.asList();
			if (!(r.size() > 0)) {
				dbStatus = new DBStatus(dbName, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), false);
				datastore.save(dbStatus);
			} else {
				dbStatus = r.get(0);
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
		return this.dbStatus.isIndexed();
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