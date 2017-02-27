import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.creation.JDTVisitor;
import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexSocket;
import indexing.IndexingSocket;
import org.javatuples.Pair;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.logging.jdk.JDKLoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 27/02/2017.
 */
public class SocketSearcher extends IndexingProject {

    public SocketSearcher(String name) {
        super(name);

    }

    public SocketSearcher() {
    }

    /**
     * Start the indexing from the <b>base_path</b> passed as parameter.
     * It iterates on the directory and sub-directories searching for Java files.
     * @param base_path	Path from where start to search for Java files.
     * @return	The number of file parsed. <u>IT IS NOT THE NUMBER OF CLASSES INSERTED IN THE DATABASE</u>
     */
    public int indexSocket(String base_path, boolean deleteOld){
        if(deleteOld) super.delete();
        Iterator i = getJavaFiles(base_path);
        int n_file = 0;
        List<String> types = new ArrayList<>();
        for(String baseType : IndexingSocket.getSocketBaseTypes()){
            List<IndexData> out = db.getType(baseType);
            for(IndexData d : out){
                if(!types.contains(d.getClassName())){
                    types.add(d.getClassName());
                }
            }
        }
        while (i.hasNext()) {
            String filename = ((File)i.next()).getAbsolutePath();
            if(this.skipTest && filename.contains("/test")){
                continue;
            }
            List<ASTClass> result = JDTVisitor.parse(filename);
            //pp filename
            for(ASTClass c : result){
                IndexingSocket indexing = new IndexingSocket(db, types);
                indexing.index(c);
                //System.out.println( Arrays.toString(indexing.getSocketTypes().toArray()) );
            }
            n_file++;
        }
        //ensure indexes
        db.ensureIndexes();
        return n_file;
    }

    public static void main(String[] args) {
        String basepath = "/Users/giovanni/repository/java-xal/project_eval/";
        String _NAMES_[] = {"activemq","airavata","jetty","vuze","wildfly-core"};

        for(String name : _NAMES_) {
            String path = basepath + name;
            SocketSearcher project = new SocketSearcher(name);
            project.indexProject(path, true);
            project.indexSocket(path, false);

            List<String> uniqueFiles = new ArrayList<>();
            List<Pair<String, String>> uniqueMethods = new ArrayList<>();
            List<String> uniqueUsage = new ArrayList<>();
            MongoConnector connector = MongoConnector.getInstance(name);
            List<IndexSocket> out = connector.getDatastore().createQuery(IndexSocket.class).asList();
            for (IndexSocket s : out) {
                if (!uniqueFiles.contains(s.getFilename())) {
                    uniqueFiles.add(s.getFilename());
                }
                Pair<String, String> p = new Pair<>(s.getFilename(), s.getMethodName());
                if (!uniqueMethods.contains(p)) {
                    uniqueMethods.add(p);
                }
                if (!uniqueUsage.contains(s.getFullName())) {
                    uniqueUsage.add(s.getFullName());
                }
            }
            System.out.println("[" + name + "]");
            System.out.println("Unique Usage: " + uniqueUsage.size());
            System.out.println("Unique Method Usage: " + uniqueMethods.size());
            System.out.println("Unique File Usage: " + uniqueFiles.size());
        }
    }
}
