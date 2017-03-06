package smellDetector;

import intermediateModelHelper.indexing.IndexingProject;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.mongoConnector.MongoOptions;
import intermediateModelHelper.indexing.structure.IndexSyncBlock;
import org.javatuples.Pair;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by giovanni on 02/03/2017.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Main eval = new Main();
        int i = 0;
        while(i < args.length){
            String current = args[i++];
            switch (current){
                case "-index":
                    eval.setIndexed(true);
                    break;
                case "-path":
                    eval.setBase_path(args[i++]);
                    break;
                case "-name":
                    eval.setName(args[i++]);
                    break;
            }
        }
        eval.run();
    }

    String name = "vuze";
    boolean isIndexed = false;
    String base_path = "";
    List<String> alreadyVisited = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public void setIndexed(boolean indexed) {
        isIndexed = indexed;
    }

    public String getBase_path() {
        return base_path;
    }

    public void setBase_path(String base_path) {
        this.base_path = base_path;
    }



    public void run(){
        double start_run = new Date().getTime();
        System.out.println("\n[Working with: " + name + "]");
        System.out.println("[path: " + this.base_path + "]");
        MongoOptions.getInstance().setDbName(name);
        //set indexing
        IndexingProject indexing = new IndexingProject(name);
        //indexing.indexProject(base_path, true);
        //indexing.indexSyncBlock(base_path, false);
        double end = new Date().getTime();
        System.out.println("[Indexing] "+ (end-start_run)/1000 + " s");

        Datastore mongo = MongoConnector.getInstance(name).getDatastore();
        Query<IndexSyncBlock> qBlock = mongo.createQuery(IndexSyncBlock.class);
        List<IndexSyncBlock> syncsblock = qBlock.asList();
        alreadyVisited.clear();
        //int total = methodsBlock.size() * methodsBlock.size();
        //int current = 0;
        for(IndexSyncBlock first : syncsblock){
            for(IndexSyncBlock second : syncsblock){
                    compare(first, second);
            }
        }
        end = new Date().getTime();
        System.out.println("[END] "+ (end-start_run)/1000 + " s");
    }

    private void compare(IndexSyncBlock first, IndexSyncBlock second) {
        boolean isSmelly = CheckSmell.isSmell(first,second);

        if(isSmelly) {
            String v1 = first.getPath() + ":" + second.getPath();
            String v2 = second.getPath()+ ":" + first.getPath();
            if(alreadyVisited.contains(v1) || alreadyVisited.contains(v2))
                return;
            alreadyVisited.add(v1);
            System.out.println(String.format("Smell @ %s:%d :: %s:%d", first.getPath() , first.getLine(), second.getPath(), second.getLine()));
        }
    }

}
