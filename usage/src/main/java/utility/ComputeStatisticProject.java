package utility;

import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 31/05/2017.
 */
public class ComputeStatisticProject {

    class countMethod extends DefaultASTVisitor {
        int c = 0;

        @Override
        public void enterASTMethod(ASTMethod elm) {
            c++;
        }

        @Override
        public void enterASTConstructor(ASTConstructor elm) {
            c++;
        }

        public int count(ASTClass z) {
            c = 0;
            z.visit(this);
            return c;
        }
    }

    public ComputeStatisticProject(String path, String name) throws IOException {
        Iterator<File> i = IndexingProject.getJavaFiles(path);
        int n_file = 0;
        int n_class = 0;
        int n_method = 0;
        int n_class_method = 0;
        countMethod counter = new countMethod();
        System.out.println("Project: " + name);
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            List<ASTClass> result = JDTVisitor.parseSpecial(filename, path, true);
            for(ASTClass c : result){
                n_class++;
                n_class_method += c.getMethods().size();
                n_method += counter.count(c);
            }
            n_file++;
        }
        System.out.println("#File:        " + n_file);
        System.out.println("#Class:       " + n_class);
        System.out.println("#Method:      " + n_method);
        System.out.println("#ClassMethod: " + n_class_method);
        System.out.println("#T Method: " + n_class_method);

    }

    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.out.println("Usage with: project_path project_name");
            System.exit(1);
        }
        String path = args[0];
        String name = args[1];
        new ComputeStatisticProject(path, name);
    }
}
