package daikon;

import daikon.annotation.WatchingPoints;

public class ComputeAnnotationPerFile {
    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage with the following three paths: [file] [project] [csv]");
            System.exit(0);
        }
        String file = args[0];
        String prj  = args[1];
        String csv  = args[2];
        WatchingPoints watchingPoints = ComputeAnnotation.get(file, prj);
        watchingPoints.toCSV(csv);
    }
}
