package daikon;

import daikon.annotation.WatchingPoints;
import debugger.Debugger;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class ComputeAnnotationPerProject {

    static class Stats {
        long methods = 0;
        long start = 0;
        long end = 0;
        long classes = 0;

        public String toString(){
            return "# (Time) Methods: " + methods +
                    "\n#Classes: " + classes +
                    "\nTime Needed: " + (end - start);
        }
    }

    private static final String cnst = "src/main/java/";
    private static Debugger d = Debugger.getInstance();
    public static void main(String[] args) throws IOException {
        if(args.length != 3){
            System.err.println("Usage with the following four paths: [projectPath] [intermediateModel.types.csv] [output]");
            System.exit(0);
        }

        Stats stats = new Stats();


        String root_path = args[0];
        String typ = args[1];
        TemporalInfo.getInstance().loadUserTypes(typ);
        String output = args[2];
        if(!output.endsWith("/"))
            output += "/";
        // Get all file
        String name = root_path.substring(root_path.lastIndexOf("/"));
        d.setName(name);


        stats.start = System.currentTimeMillis();

        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(!filename.contains(cnst)) continue;
            String className = filename.substring(
                    filename.lastIndexOf(cnst) + cnst.length(),
                    filename.lastIndexOf(".")
                ).replace("/",".");
            d.log(filename);
            WatchingPoints watchingPoints = ComputeAnnotation.get(filename, root_path);
            if(watchingPoints.size() > 0) {
                //System.out.println(output + className + ".csv");
                //compute stats
                stats.classes += watchingPoints.getClassSize();
                stats.methods += watchingPoints.getMethodSize();
                watchingPoints.toCSV(output + className + ".csv");
            }
        }

        stats.end = System.currentTimeMillis();
        d.log(stats.toString());
        System.out.println(stats);
        d.stop();
    }
}
