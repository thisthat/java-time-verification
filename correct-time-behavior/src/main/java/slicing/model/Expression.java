package slicing.model;

import intermediateModel.interfaces.IASTRE;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

/**
 * Created by giovanni on 11/07/2017.
 */
public class Expression extends Stm {

    IASTRE expr = null;

    public Expression(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public Expression(IASTRE stm) {
        super(  stm.getStart(),
                stm.getEnd(),
                stm.getLine(),
                stm.getLineEnd(),
                stm.getCode()
                );
    }

    public IASTRE getExpr() {
        return expr;
    }

    public void setExpr(IASTRE expr) {
        this.expr = expr;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterExpression(this);
        if(expr != null)
            expr.visit(visitor);
        visitor.exitExpression(this);
    }

    @Override
    public String toString() {
        return expr.print();
    }
}
