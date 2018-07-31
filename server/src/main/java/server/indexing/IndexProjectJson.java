package server.indexing;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import server.helper.PrepareJsonClass;
import server.helper.SHA1;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static intermediateModelHelper.indexing.IndexingProject.getJavaFiles;

public class IndexProjectJson {
    protected MongoConnectorServer db;
    protected String projectName;

    public IndexProjectJson(String name) {
        this.db = MongoConnectorServer.getInstance(name);
        this.projectName = name;
    }

    public void indexProject(String base_path, boolean deleteOld){
        //remove old preprocess
        db.setIndexStart();
        if(deleteOld) delete();
        Iterator i = getJavaFiles(base_path);
        int n_file = 0;
        while (i.hasNext()) {
            String filename = ((File)i.next()).getAbsolutePath();
            List<ASTClass> result = JDTVisitor.parse(filename, base_path);
            index(result, filename, base_path);
        }
        //ensure indexes
        db.ensureIndexes();
        db.setIndexFinish();
        db.setBasePath(base_path);
    }

    private void index(List<ASTClass> result, String path, String base_path) {
        DBDataJSON j = new DBDataJSON();
        j.setPath(path);
        j.setJson(PrepareJsonClass.json(result, new HashMap<>(), path, base_path));
        j.setSha1(SHA1.calcate(path));
        db.add(j);
    }

    public void delete(){
        db.drop();
    }
}
