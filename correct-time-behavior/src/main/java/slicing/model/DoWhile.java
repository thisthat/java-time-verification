package slicing.model;

import intermediateModel.interfaces.IASTStm;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

/**
 * Created by giovanni on 11/07/2017.
 */
public class DoWhile extends While {


    public DoWhile(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public DoWhile(IASTStm stm) {
        super(  stm.getStart(),
                stm.getEnd(),
                stm.getLine(),
                stm.getLineEnd(),
                stm.getCode()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoWhile aWhile = (DoWhile) o;
        if(!super.equals(o)) return false;
        if (expr != null ? !expr.equals(aWhile.expr) : aWhile.expr != null) return false;
        return whileBody != null ? whileBody.equals(aWhile.whileBody) : aWhile.whileBody == null;
    }

    @Override
    public int hashCode() {
        int result = expr != null ? expr.hashCode() : 0;
        result = 31 * result + (whileBody != null ? whileBody.hashCode() : 0);
        return result;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterDoWhile(this);
        if(expr != null)
            expr.visit(visitor);
        for(Stm s : whileBody){
            s.visit(visitor);
        }
        visitor.exitDoWhile(this);
    }

}
