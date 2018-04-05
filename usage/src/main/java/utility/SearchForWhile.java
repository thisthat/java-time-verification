package utility;

import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTWhile;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModel.visitors.creation.filter.Filter;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 31/05/2017.
 */
public class SearchForWhile {

    public SearchForWhile(String path, String name) throws IOException {
        //System.out.println("Project: " + name);
        //System.out.println("Path: " + path);
        Iterator<File> i = IndexingProject.getJavaFiles(path);
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            List<ASTClass> result = JDTVisitor.parse(filename, path, ElseIf.filter);

            for(ASTClass c : result){
                //compute time
                ApplyHeuristics.getConstraintV2(c);
                c.visit(new DefaultASTVisitor(){
                    @Override
                    public void enterASTWhile(ASTWhile elm) {
                        if(elm.getExpr().isTimeCritical()){
                            String className = c.fullName();
                            int line = elm.getLine();
                            String p = c.getPath();
                            System.out.println(String.format("%s;%d;%s", className, line, p));
                        }
                    }
                });
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 3) {
            System.out.println("Usage with: project_path project_name types");
            System.exit(1);
        }
        String path = args[0];
        String name = args[1];
        String typ  = args[2];
        TemporalInfo.getInstance().loadUserTypes(typ);
        new SearchForWhile(path, name);
    }
}
