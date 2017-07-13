package slicing.model;

import intermediateModel.interfaces.IASTStm;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 11/07/2017.
 */
public class While extends Stm {

    Stm expr;
    List<Stm> whileBody = new ArrayList<>();

    public While(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public While(IASTStm stm) {
        super(  stm.getStart(),
                stm.getEnd(),
                stm.getLine(),
                stm.getLineEnd(),
                stm.getCode()
        );
    }

    public Stm getExpr() {
        return expr;
    }

    public void setExpr(Stm expr) {
        this.expr = expr;
    }

    public List<Stm> getWhileBody() {
        return whileBody;
    }

    public void setWhileBody(List<Stm> whileBody) {
        this.whileBody = whileBody;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterWhile(this);
        if(expr != null)
            expr.visit(visitor);
        for(Stm s : whileBody){
            s.visit(visitor);
        }
        visitor.exitWhile(this);
    }
}
