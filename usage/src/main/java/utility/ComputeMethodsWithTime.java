package utility;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 31/05/2017.
 */
public class ComputeMethodsWithTime {

    public ComputeMethodsWithTime(String path) {
        Iterator<File> i = IndexingProject.getJavaFiles(path);
        int n_file = 0;
        int n_class = 0;
        int n_method = 0;
        int n_method_time = 0;

        System.out.println("Project: " + path);
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(!filename.equals("/Users/giovanni/repository/kafka-4426/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/ConsumerCoordinator.java"))
                continue;
            List<ASTClass> result = JDTVisitor.parse(filename, path, false);
            for(ASTClass c : result){
                n_class++;
                n_method += c.getCountMethod();
                //compute time
                ApplyHeuristics.getConstraintV2(c);
                System.out.println(c);
            //
            }
            n_file++;
        }
        System.out.println("#File:        " + n_file);
        System.out.println("#Class:       " + n_class);
        System.out.println("#Method:      " + n_method);

    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            System.out.println("Usage with: project_path ");
            System.exit(1);
        }
        String path = args[0];
        new ComputeMethodsWithTime(path);
    }
}
