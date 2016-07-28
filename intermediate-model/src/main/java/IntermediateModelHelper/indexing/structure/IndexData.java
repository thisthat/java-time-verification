package IntermediateModelHelper.indexing.structure;

import com.google.common.annotations.Beta;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
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
	//@Reference
	List<IndexMethod> listOfMethods = new ArrayList<>();
	@Beta
	List<String> listOfTimedMethods = new ArrayList<>();
	//@Reference
	List<IndexMethod> listOfSyncMethods = new ArrayList<>();
	//@Reference
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
