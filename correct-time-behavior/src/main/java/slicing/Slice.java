package slicing;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import slicing.heuristics.AssignmentTimeVar;
import slicing.heuristics.MarkTime;
import slicing.heuristics.TimeInSignature;

/**
 * Created by giovanni on 06/07/2017.
 */
public class Slice {

    static ApplyHeuristics ah = new ApplyHeuristics();
    static {
        ah.subscribe(MarkTime.class);
        ah.subscribe(TimeInSignature.class);
        ah.subscribe(AssignmentTimeVar.class);
    }

    public static void slice(ASTClass c) {
        ah.analyze(c);
    }
}
