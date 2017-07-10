package slicing;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import slicing.heuristics.*;

/**
 * Created by giovanni on 06/07/2017.
 */
public class Slice {

    static ApplyHeuristics ah = new ApplyHeuristics();
    static {
        ah.subscribe(MarkTime.class);
        ah.subscribe(MinMaxSearch.class);
        ah.subscribe(TimeInSignature.class);
        ah.subscribe(AssignmentTimeVar.class);
        ah.subscribe(PrintTimeVar.class);
    }

    public static void slice(ASTClass c) {
        TimeStatements timeStms = TimeStatements.getInstance();
        timeStms.clear();
        ah.analyze(c);

    }
}
