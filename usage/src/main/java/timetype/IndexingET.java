package timetype;

import debugger.Debugger;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.envirorment.temporal.structure.TimeMethod;
import intermediateModelHelper.envirorment.temporalTypes.TemporalTypes;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.IOException;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class IndexingET {

    public static long timeSpent, timeSpentWriting, timeSpentInit, start, end;
    static Debugger debug = Debugger.getInstance();

    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.out.println("Usage with: name root_path");
            System.exit(0);
        }
        try {
            new IndexingET().do_job(args);
        } catch (Exception e){

        } finally {
            debug.stop();
        }
    }

    public void do_job(String[] args) throws Exception {
        timeSpent = 0;
        timeSpentWriting = 0;
        //get root path
        String name = args[0];
        debug.setName(name);
        debug.setActive(false);
        String root_path = args[1];

        TemporalTypes ti = TemporalTypes.getInstance();


        ti.loadUserDefined("config/" + name);
        //index time in parameters
        {
            long s = System.currentTimeMillis();
            debug.log("Indexing intermediateModel.types of the project");
            List<TimeMethod> tpar = IndexingProject.getMethodTimeParameter(name, root_path, true);
            TemporalInfo.getInstance().addTimeInSignature(tpar);
            long e = System.currentTimeMillis();
            timeSpentInit += (e - s);
            System.out.println(String.format("Get ET %d methods", tpar.size()));
        }

        System.out.println("Initialization: " + timeSpentInit);
    }

}
