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
import sql.SQLManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class MainCommit {

    static Debugger debugger = Debugger.getInstance();

    static {
        debugger.setActive(false);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage with: hash name root_path");
            System.exit(0);
        }
        try {
            new MainCommit().do_job(args);
            debugger.stop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            debugger.stop();
        }
    }

    public void do_job(String[] args) throws Exception {

        //get root path
        String hash = args[0];
        String name = args[1];
        String root_path = args[2];

        TemporalTypes.getInstance().loadUserDefinedPrefix(name);

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

        System.out.println("Indexing - Done");

        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        int error = 0;
        int warning = 0;
        long timeParsing = 0;
        long timeProcess = 0;
        List<TimeTypeError> e = new ArrayList<>();
        List<TimeTypeRecommendation> r = new ArrayList<>();
        List<TimeTypeWarning> w = new ArrayList<>();
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
//           if(!filename.endsWith("JobSchedulerImpl.java")) continue; //skip tests
            debugger.log("Parsing: " + filename);
            long sParsing = System.currentTimeMillis();
            List<ASTClass> classes = JDTVisitor.parse(filename, root_path, ElseIf.filter);
            long eParsing = System.currentTimeMillis();
            timeParsing += (eParsing - sParsing);
            long sProcess = System.currentTimeMillis();
            for (ASTClass c : classes) {
                TimeTypeSystem tts = new TimeTypeSystem();
                tts.start(c);
                List<TimeTypeError> errors = tts.getErrors();
                List<TimeTypeWarning> warnings = tts.getWarnings();
                //e.addAll(errors);
                for(TimeTypeError tte : errors){
                    if(!e.contains(tte)){
                        e.add(tte);
                    }
                }
                for(TimeTypeRecommendation rec : tts.getRecommendation()){
                    if(!r.contains(rec)){
                        r.add(rec);
                    }
                }
                w.addAll(warnings);
                warning += warnings.size();
            }
            long eProcess = System.currentTimeMillis();
            timeProcess += (eProcess - sProcess);
        }
        List<String> err = new ArrayList<>();
        for(TimeTypeError tte : e){
            //System.out.println(tte.getFullMessage());
            err.add(tte.getFullMessage());
        }
        List<String> arr = new ArrayList<>();
        for(TimeTypeWarning ttw : w){
            arr.add(ttw.getFullMessage());
        }
        List<String> rec = new ArrayList<>();
        for(TimeTypeRecommendation rr : r){
            rec.add(rr.getFullMessage());
        }
        System.out.println("Storing Results...");
        String warnList = Arrays.toString(arr.toArray());
        String errList = Arrays.toString(err.toArray());
        String recList = Arrays.toString(rec.toArray());
        SQLManager sql = new SQLManager();
        sql.addCommitResult(hash, timeParsing, timeProcess, w.size(), err.size(), r.size(), warnList, errList, recList, name);
        sql.close();
    }

}
