package slicing.model;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.expression.ASTMethodCall;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

/**
 * Created by giovanni on 11/07/2017.
 */
public class MethodCall extends Stm {

    String pointedClass;
    IASTRE methodCall;


    public MethodCall(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public MethodCall(IASTStm stm) {
        super(  stm.getStart(),
                stm.getEnd(),
                stm.getLine(),
                stm.getLineEnd(),
                stm.getCode()
        );
    }

    public String getPointedClass() {
        return pointedClass;
    }

    public void setPointedClass(String pointedClass) {
        this.pointedClass = pointedClass;
    }

    public IASTRE getMethodCall() {
        return methodCall;
    }

    public void setMethodCall(IASTRE methodCall) {
        this.methodCall = methodCall;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterMethodCall(this);
        if(methodCall != null)
            methodCall.visit(visitor);
        visitor.exitMethodCall(this);
    }
}
