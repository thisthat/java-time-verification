package smt.evaluation;

import debugger.Debugger;
import intermediateModelHelper.envirorment.temporal.structure.TimeTypes;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.IOException;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class LibraryCheck {

    public static long timeSpent, timeSpentWriting, timeSpentInit, start, end;
    static Debugger debug = Debugger.getInstance();

    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.out.println("Usage with: name root_path");
            System.exit(0);
        }
        try {
            new LibraryCheck().do_job(args);
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
        String root_path = args[1];


        //index return times
        {
            long s = System.currentTimeMillis();
            debug.log("Indexing types of the project");
            List<TimeTypes> tt = IndexingProject.getMethodReturnTime(name + "_libraries", root_path, true);
            long e = System.currentTimeMillis();
            timeSpentInit += (e - s);
            System.out.println(String.format("Get %d methods", tt.size()));
            System.out.println("Took: " + timeSpentInit + "ms");

        }

        System.out.println("Initialization: " + timeSpentInit);
    }

}
