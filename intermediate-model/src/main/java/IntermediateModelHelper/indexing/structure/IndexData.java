package IntermediateModelHelper.indexing.structure;

import com.google.common.annotations.Beta;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The following class is used to save some data in a MongoDB.
 * The data stored for a class consists in:
 * <ul>
 *     <li>List of methods {@link IndexMethod}</li>
 *     <li>List of synchronized methods {@link IndexMethod}</li>
 *     <li>List of synchronized blocks {@link IndexSyncBlock}</li>
 *     <li><b>BETA</b> List of timed related methods</li>
 *     <li>Package of the class</li>
 *     <li>Class Name</li>
 *     <li>Extended Class</li>
 *     <li>List of implemented interfaces</li>
 * </ul>
 * We ensure that the object stored in MongoDB has two indexes on (i) Class Name and (ii) Package Name of each class.
 * The class is just a <i>Struct</i> that serves the purpose of storing information only.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */

@Entity("IndexData")
@Indexes({
		@Index(value = "className", fields = @Field("className")),
		@Index(value = "classPackage", fields = @Field("classPackage"))
})
public class IndexData {
	@Id
	private ObjectId id;
	List<IndexMethod> listOfMethods = new ArrayList<>();
	@Beta
	List<String> listOfTimedMethods = new ArrayList<>();
	List<IndexMethod> listOfSyncMethods = new ArrayList<>();
	List<IndexSyncBlock> listOfSyncBlocks = new ArrayList<>();

	String classPackage = "";
	String className = "";
	String extendedType = "";
	List<String> interfacesImplemented = new ArrayList<>();

	public IndexData() {
	}

	public IndexData(ObjectId id, List<IndexMethod> listOfMethods, List<String> listOfTimedMethods, List<IndexMethod> listOfSyncMethods, List<IndexSyncBlock> listOfSyncBlocks, String classPackage, String className, String extendedType, List<String> interfacesImplemented) {
		this.id = id;
		this.listOfMethods = listOfMethods;
		this.listOfTimedMethods = listOfTimedMethods;
		this.listOfSyncMethods = listOfSyncMethods;
		this.listOfSyncBlocks = listOfSyncBlocks;
		this.classPackage = classPackage;
		this.className = className;
		this.extendedType = extendedType;
		this.interfacesImplemented = interfacesImplemented;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public List<IndexMethod> getListOfMethods() {
		return listOfMethods;
	}

	public void setListOfMethods(List<IndexMethod> listOfMethods) {
		this.listOfMethods = listOfMethods;
	}

	public List<String> getListOfTimedMethods() {
		return listOfTimedMethods;
	}

	public void setListOfTimedMethods(List<String> listOfTimedMethods) {
		this.listOfTimedMethods = listOfTimedMethods;
	}

	public List<IndexMethod> getListOfSyncMethods() {
		return listOfSyncMethods;
	}

	public void setListOfSyncMethods(List<IndexMethod> listOfSyncMethods) {
		this.listOfSyncMethods = listOfSyncMethods;
	}

	public List<IndexSyncBlock> getListOfSyncBlocks() {
		return listOfSyncBlocks;
	}

	public void setListOfSyncBlocks(List<IndexSyncBlock> listOfSyncBlocks) {
		this.listOfSyncBlocks = listOfSyncBlocks;
	}

	public String getClassPackage() {
		return classPackage;
	}

	public void setClassPackage(String classPackage) {
		this.classPackage = classPackage;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getExtendedType() {
		return extendedType;
	}

	public void setExtendedType(String extendedType) {
		this.extendedType = extendedType;
	}

	public List<String> getInterfacesImplemented() {
		return interfacesImplemented;
	}

	public void setInterfacesImplemented(List<String> interfacesImplemented) {
		this.interfacesImplemented = interfacesImplemented;
	}

	public void addMethod(IndexMethod m){
		this.listOfMethods.add(m);
	}

	public void addSyncMethod(IndexMethod m) {
		this.listOfSyncMethods.add(m);
	}

	public void addSyncBlock(IndexSyncBlock b) {
		this.listOfSyncBlocks.add(b);
	}
}
