package types;

import debugger.Debugger;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.typedefinition.TimeType;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.interfaces.ParseIM;
import intermediateModelHelper.envirorment.Env;
import slicing.heuristics.*;
import types.rules.TimeTypeError;
import types.rules.TypeResolver;

import java.util.ArrayList;
import java.util.List;

public class TimeTypeSystem extends ParseIM  {

    static ApplyHeuristics ah = new ApplyHeuristics();
    static {
        //ah.set__DEBUG__(true);
        ah.subscribe(MarkTime.class);
        ah.subscribe(TimeInSignature.class);
        ah.subscribe(AssignmentTimeVar.class);
        ah.subscribe(BooleanExpression.class);
        ah.subscribe(MinMaxSearch.class);
        ah.subscribe(ReturnExpression.class);
        ah.subscribe(AddTimeVarToTimeExpression.class);
    }

    List<TimeTypeError> errors = new ArrayList<>();

    public List<TimeTypeError> getErrors() {
        return errors;
    }

    @Override
    public void start(ASTClass c){
        errors.clear();
        super.start(c);
    }

    @Override
    protected void analyzeASTRE(ASTRE r, Env env) {
        if(r.getExpression() == null){
            return;
        }
        try {
            TimeType t = TypeResolver.resolveTimerType(r.getExpression(), env);
            //Debugger.getInstance(false).log("Line " + r.getLine() + " : " + t);
        } catch (TimeTypeError error){
            // catch the error and enhance it. Then store.
            errors.add(new TimeTypeError(super.getLastClass(), super._class.getPath(), error));
        }
    }


}
