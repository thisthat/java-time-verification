package utility;

import intermediateModel.structure.ASTClass;
import intermediateModel.structure.expression.ASTMethodCall;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.ParseTypes;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.indexing.IndexingProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.UnparsableException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 31/05/2017.
 */
public class ComputeJavaVsLib {

    public ComputeJavaVsLib(String name, String path, String jars, String user, String libs) throws IOException {
        // construct classpath - jars + /main/src/ folders
        List<String> jar_path = Java2AST.getJars(jars);
        jar_path.addAll( Java2AST.getClassPath(path) );


        // Load java-libs time apis
        List<TimeTypes> java_api = TemporalInfo.getInstance().getTimeTypes();
        java_api.addAll(new ParseTypes( user ).getMethods());

        List<TimeTypes> lib_api = new ParseTypes( libs ).getMethods();


        System.out.println("Project: " + name);
        final long[] java_calls = {0};
        final long[] libs_calls = {0};

        Iterator<File> i = IndexingProject.getJavaFiles(path);

        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            Java2AST a = null;
            try {
                a = new Java2AST(filename, true, path, jar_path);
            } catch (UnparsableException e) {
                continue;
            }
            CompilationUnit r = a.getContextJDT();
            JDTVisitor v = new JDTVisitor(r, filename);
            r.accept(v);
            List<ASTClass> result = v.listOfClasses;
            for(ASTClass c : result){
                c.visit(new DefaultASTVisitor(){
                    @Override
                    public void enterASTMethodCall(ASTMethodCall elm) {
                        for(TimeTypes u : java_api){
                            if(u.isMethodCall(elm)) {
                                java_calls[0]++;
                                return;
                            }
                        }
                        for(TimeTypes u : lib_api){
                            if(u.isMethodCall(elm)) {
                                libs_calls[0]++;
                                return;
                            }
                        }
                    }
                });
            }

            System.out.println("Java Time Calls: " + java_calls[0]);
            System.out.println("Libs Time Calls: " + libs_calls[0]);
        }

    }

    public static void main(String[] args) throws IOException {
        if(args.length < 5) {
            System.out.println("Usage with: project_name project_path lib_path user_types lib_types");
            System.exit(1);
        }
        String path = args[0];
        String name = args[1];
        String jars = args[2];
        String user = args[3];
        String lib = args[4];
        new ComputeJavaVsLib(path, name, jars, user, lib);
    }
}
