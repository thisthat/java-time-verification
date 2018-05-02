package timetype;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.indexing.IndexingProject;
import types.TimeTypeSystem;
import types.rules.TimeTypeError;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Main {


    public static void main(String[] args) throws IOException {
        if(args.length < 1){
            System.out.println("Usage with: root_path");
            System.exit(0);
        }
        try {
            new Main().do_job(args);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void do_job(String[] args) throws Exception {

        //get root path
        String root_path = args[0];
        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        int total = 0;
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
            List<ASTClass> classes = JDTVisitor.parse(filename, root_path, ElseIf.filter);
            for(ASTClass c : classes){
                TimeTypeSystem tts = new TimeTypeSystem();
                tts.start(c);
                List<TimeTypeError> errors = tts.getErrors();
                total += errors.size();
                for(TimeTypeError err : errors){
                    System.err.println(err.getFullMessage());
                }
            }

        }

    }

}
