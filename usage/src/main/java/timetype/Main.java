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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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

    public static void main(String[] args) {
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
            System.out.println("@ " + debugger.getLastFile());
            debugger.stop();
        }
    }

    public Main() {
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

        List<TimeTypeRecommendation> r = new ArrayList<>();
        List<TimeTypeError> e = new ArrayList<>();
        List<TimeTypeWarning> w = new ArrayList<>();
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(filename.contains("/src/test/")) continue; //skip tests
//            if(!filename.endsWith("KafkaConsumer.java")) continue; //skip tests
            debugger.log("Parsing: " + filename);
            debugger.setLastFile(filename);
            //System.out.println(filename);
            List<ASTClass> classes = JDTVisitor.parse(filename, root_path, ElseIf.filter);
            for (ASTClass c : classes) {
                TimeTypeSystem tts = new TimeTypeSystem();
                tts.start(c);
                List<TimeTypeError> errors = tts.getErrors();
                List<TimeTypeWarning> warnings = tts.getWarnings();
                //e.addAll(errors);
                for(TimeTypeError tte : errors){
                    if(!e.contains(tte)){
                        e.add(tte);
                        //System.out.println(tte.getFullMessage());
                    }
                }
                for(TimeTypeRecommendation rec : tts.getRecommendation()){
                    if(!r.contains(rec)){
                        r.add(rec);
                        //System.out.println(tte.getFullMessage());
                    }
                }
                w.addAll(warnings);
            }
        }
        //System.exit(0);
        long end = System.currentTimeMillis();
        System.out.println("Total Time [ms]: " + (end - start));
        System.out.println("Total # Errors : " + e.size());
        System.out.println("Total # Recoms : " + r.size());
        System.out.println("Total # Warnings : " + w.size());
        System.out.println("======= ERRORS =======");
        for(TimeTypeError err : e){
            System.out.println(err.getFullMessage());
        }
        System.out.println("======= WARNING ======");
        for(TimeTypeWarning warn : w){
           // System.out.println(warn.getFullMessage());
        }
        BufferedWriter writerErr = new BufferedWriter(new FileWriter("errors.log", true));
        BufferedWriter writerRec = new BufferedWriter(new FileWriter("recommendation.log", true));
        BufferedWriter writerWarn = new BufferedWriter(new FileWriter("warnings.log", true));
        for(TimeTypeError err : e){
            writerErr.append(err.getFullMessage());
            writerErr.append("\n");
        }
        for(TimeTypeRecommendation rec : r){
            writerRec.append(rec.getFullMessage());
            writerRec.append("\n");
        }
        for(TimeTypeWarning warn : w){
            writerWarn.append(warn.getFullMessage());
            writerErr.append("\n");
        }
        writerErr.flush();
        writerErr.close();
        writerWarn.flush();
        writerWarn.close();
        writerRec.flush();
        writerRec.close();
    }

}
