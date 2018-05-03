package timetype;

import debugger.Debugger;
import intermediateModel.structure.ASTClass;
import intermediateModel.types.rules.TimeTypeWarning;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.envirorment.temporalTypes.TemporalTypes;
import intermediateModelHelper.indexing.IndexingProject;
import intermediateModel.types.TimeTypeSystem;
import intermediateModel.types.rules.TimeTypeError;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Main {

    static Debugger debugger = Debugger.getInstance();

    static {
        debugger.setActive(false);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage with: name root_path");
            System.exit(0);
        }
        try {
            new Main().do_job(args);
            debugger.stop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            debugger.stop();
        }
    }

    public void do_job(String[] args) throws Exception {

        //get root path
        String name = args[0];
        String root_path = args[1];

        TemporalTypes ti = TemporalTypes.getInstance();
        ti.loadUserDefined("config/" + name);

        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        int total = 0;
        int warning = 0;
        long start = System.currentTimeMillis();
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
            //if(!filename.equals("/Users/giovanni/repository/kafka/clients/src/main/java/org/apache/kafka/clients/consumer/internals/ConsumerNetworkClient.java")) continue; //skip tests
            debugger.log("Parsing: " + filename);
            List<ASTClass> classes = JDTVisitor.parse(filename, root_path, ElseIf.filter);
            for (ASTClass c : classes) {
                TimeTypeSystem tts = new TimeTypeSystem();
                tts.start(c);
                List<TimeTypeError> errors = tts.getErrors();
                List<TimeTypeWarning> warnings = tts.getWarnings();
                total += errors.size();
                warning += warnings.size();
                for (TimeTypeError err : errors) {
                    System.err.print("\t");
                    System.err.println(err.getFullMessage());
                }

                for (TimeTypeWarning warn : warnings) {
                    System.out.print("\t");
                    System.out.println(warn.getFullMessage());
                }
            }

        }
        long end = System.currentTimeMillis();
        System.out.println("Total Error: " + total);
        System.out.println("Total Warning: " + warning);
        System.out.println("Took: " + (end-start));

    }

}
