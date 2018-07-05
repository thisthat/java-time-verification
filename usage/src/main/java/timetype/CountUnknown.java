package timetype;

import debugger.Debugger;
import intermediateModel.structure.ASTClass;
import intermediateModel.types.TimeTypeSystem;
import intermediateModel.types.rules.exception.TimeTypeError;
import intermediateModel.types.rules.exception.TimeTypeRecommendation;
import intermediateModel.types.rules.exception.TimeTypeWarning;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.envirorment.temporalTypes.TemporalTypes;
import intermediateModelHelper.envirorment.temporalTypes.structure.TimeParameterMethod;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class CountUnknown {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage with: name root_path");
            System.exit(0);
        }
        try {
            new CountUnknown().do_job(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public CountUnknown() {
        super();
    }

    public void do_job(String[] args) throws Exception {

        //get root path
        String name = args[0];
        String root_path = args[1];
        boolean index = args.length <= 2 || args[2].equals("y");

        long start = System.currentTimeMillis();

        TemporalTypes.getInstance().loadUserDefinedPrefix(name);

        if(index) {
            // Indexing RT
            {
                List<TimeTypes> rt = IndexingProject.getMethodReturnTime(name, root_path, true);
                TemporalTypes.getInstance().addRT(rt);
                TemporalTypes.getInstance().loadUserDefinedPrefix(name);
                List<TimeTypes> rt1 = IndexingProject.getMethodReturnTime(name, root_path, true);
                TemporalTypes.getInstance().addRT(rt1);
                TemporalTypes.getInstance().loadUserDefinedPrefix(name);
            }
            // Indexing ET
            {
                List<TimeParameterMethod> et = IndexingProject.getMethodTimeParameter(name, root_path, true);
                TemporalTypes.getInstance().addET(et);
                TemporalTypes.getInstance().loadUserDefinedPrefix(name);
            }

        }
        System.out.println("Indexing - Done");

        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        long typed = 0;
        long untyped = 0;
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
            List<ASTClass> classes = JDTVisitor.parse(filename, root_path, ElseIf.filter);
            for (ASTClass c : classes) {
                intermediateModel.types.CountUnknown tts = new intermediateModel.types.CountUnknown();
                tts.start(c);
                typed += tts.getTyped().size();
                untyped += tts.getUntyped().size();
            }
        }
        //System.exit(0);
        long end = System.currentTimeMillis();
        System.out.println("Total Time [ms]: " + (end - start));
        System.out.println("Total # Typed : " + typed);
        System.out.println("Total # Untyped : " + untyped);

    }

}
