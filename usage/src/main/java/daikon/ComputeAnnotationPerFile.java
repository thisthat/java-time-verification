package daikon;

import daikon.annotation.WatchingPoints;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;

public class ComputeAnnotationPerFile {
    public static void main(String[] args) {
        if(args.length != 4){
            System.err.println("Usage with the following four paths: [file] [project] [intermediateModel.types.csv] [csv]");
            System.exit(0);
        }
        String file = args[0];
        String prj  = args[1];
        String typ  = args[2];
        TemporalInfo.getInstance().loadUserTypes(typ);
        String csv  = args[3];
        WatchingPoints watchingPoints = ComputeAnnotation.get(file, prj);
        watchingPoints.toCSV(csv);
    }
}
