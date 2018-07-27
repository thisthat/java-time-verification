package server.indexing;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

@Entity("DBDataJSON")
@Indexes(
        @Index(fields = @Field(value = "$**", type = IndexType.TEXT))
)
public class DBDataJSON {
    @Id
    private ObjectId id;
    String path;
    String json;
    String sha1;

    public DBDataJSON() {
    }

    public DBDataJSON(ObjectId id, String path, String json, String sha1) {
        this.id = id;
        this.path = path;
        this.json = json;
        this.sha1 = sha1;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
}
