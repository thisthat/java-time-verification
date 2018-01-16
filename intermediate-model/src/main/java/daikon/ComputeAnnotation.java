package daikon;

import daikon.annotation.TimeVarCollector;
import daikon.annotation.WatchingPoints;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;

import java.util.List;

public class ComputeAnnotation {
    public static WatchingPoints get(String f){
        return get(f, f.substring(0, f.lastIndexOf("/")));
    }

    public static WatchingPoints get(String f, String p) {
        WatchingPoints out = new WatchingPoints();

        List<ASTClass> cc = JDTVisitor.parse(f, p);
        for (ASTClass c : cc) {
            ApplyHeuristics.getConstraintV2(c);
            TimeVarCollector timeVarCollector = new TimeVarCollector();
            out.add(timeVarCollector.getTimeVariables(c));
        }
        return out;
    }
}
