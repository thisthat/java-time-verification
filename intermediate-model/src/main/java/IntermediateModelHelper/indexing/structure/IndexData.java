package IntermediateModelHelper.indexing.structure;

import com.google.common.annotations.Beta;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

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
@Indexes(
		@Index(fields = @Field(value = "$**", type = IndexType.TEXT))
)
public class IndexData {
	@Id
	private ObjectId id;
	List<IndexMethod> listOfMethods = new ArrayList<>();
	@Beta
	List<String> listOfTimedMethods = new ArrayList<>();
	List<IndexMethod> listOfSyncMethods = new ArrayList<>();
	List<IndexSyncBlock> listOfSyncBlocks = new ArrayList<>();
	List<IndexParameter> timeAttribute = new ArrayList<>();
	List<String> imports = new ArrayList<>();

	String classPackage = "";
	String name = "";
	String extendedType = "";
	String fullName = "";
	String path = "";
	List<String> interfacesImplemented = new ArrayList<>();

	public IndexData() {
	}

	public IndexData(ObjectId id, List<IndexMethod> listOfMethods, List<String> listOfTimedMethods, List<IndexMethod> listOfSyncMethods, List<IndexSyncBlock> listOfSyncBlocks, List<IndexParameter> timeAttribute, List<String> imports, String classPackage, String name, String extendedType, String fullName, String path, List<String> interfacesImplemented) {
		this.id = id;
		this.listOfMethods = listOfMethods;
		this.listOfTimedMethods = listOfTimedMethods;
		this.listOfSyncMethods = listOfSyncMethods;
		this.listOfSyncBlocks = listOfSyncBlocks;
		this.timeAttribute = timeAttribute;
		this.imports = imports;
		this.classPackage = classPackage;
		this.name = name;
		this.extendedType = extendedType;
		this.fullName = fullName;
		this.path = path;
		this.interfacesImplemented = interfacesImplemented;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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
		return name;
	}

	public void setClassName(String className) {
		this.name = className;
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

	public List<IndexParameter> getTimeAttribute() {
		return timeAttribute;
	}

	public void setTimeAttribute(List<IndexParameter> timeAttribute) {
		this.timeAttribute = timeAttribute;
	}

	public List<String> getImports() {
		return imports;
	}

	public void setImports(List<String> imports) {
		this.imports = imports;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "IndexData{" +
				"classPackage='" + classPackage + '\'' +
				", className='" + name + '\'' +
				"}\n";
	}
}
