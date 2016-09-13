package IntermediateModelHelper.indexing.structure;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
@Entity("IndexSyncCall")
@Indexes(
		@Index(fields = @Field(value = "$**", type = IndexType.TEXT))
)
public class IndexSyncCall {
	@Id
	private ObjectId id;
	private String _packageName;
	private String _className;
	private String _inMethodName;
	private List<String> _signatureInMethod;

	public IndexSyncCall() {
	}

	public IndexSyncCall(ObjectId id, String _packageName, String _className, String _inMethodName, List<String> _signatureInMethod) {
		this.id = id;
		this._packageName = _packageName;
		this._className = _className;
		this._inMethodName = _inMethodName;
		this._signatureInMethod = _signatureInMethod;
	}

	public IndexSyncCall(SyncMethodCall call) {
		this._packageName = call.get_packageName();
		this._className = call.get_className();
		this._inMethodName = call.get_inMethodName();
		this._signatureInMethod = call.get_signatureInMethod();
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String get_packageName() {
		return _packageName;
	}

	public void set_packageName(String _packageName) {
		this._packageName = _packageName;
	}

	public String get_className() {
		return _className;
	}

	public void set_className(String _className) {
		this._className = _className;
	}

	public String get_inMethodName() {
		return _inMethodName;
	}

	public void set_inMethodName(String _inMethodName) {
		this._inMethodName = _inMethodName;
	}

	public List<String> get_signatureInMethod() {
		return _signatureInMethod;
	}

	public void set_signatureInMethod(List<String> _signatureInMethod) {
		this._signatureInMethod = _signatureInMethod;
	}
}
