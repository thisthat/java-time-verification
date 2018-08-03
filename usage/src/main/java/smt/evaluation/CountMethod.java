package smt.evaluation;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class CountMethod {

    public static void main(String[] args) throws IOException {
        if(args.length < 3){
            System.out.println("Usage with: name root_path output_path");
            System.exit(0);
        }
        try {
            new CountMethod().do_job(args);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void do_job(String[] args) throws Exception {
        //get root path
        String name = args[0];
        String root_path = args[1];

        System.out.println("Name: " + name);

        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);

        //stats
        int nMethod = 0;

        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
            //each class
            List<ASTClass> result = JDTVisitor.parseSpecial(filename, root_path, false);
            for(ASTClass c : result){
                nMethod = nMethod + c.getCountMethod();
            }
        }
        System.out.println("\n# Methods: " + nMethod);
    }

}
