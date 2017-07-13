package slicing.model;

import intermediateModel.structure.ASTIf;
import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 11/07/2017.
 */
public class If extends Stm {

    Stm expr = null;
    List<Stm> ifBody = new ArrayList<>();
    List<Stm> elseBody = new ArrayList<>();

    public If(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public If(ASTIf stm) {
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

    public List<Stm> getIfBody() {
        return ifBody;
    }

    public void setIfBody(List<Stm> ifBody) {
        this.ifBody = ifBody;
    }

    public List<Stm> getElseBody() {
        return elseBody;
    }

    public void setElseBody(List<Stm> elseBody) {
        this.elseBody = elseBody;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        visitor.enterIf(this);
        if(expr != null){
            expr.visit(visitor);
        }
        for(Stm s : ifBody){
            s.visit(visitor);
        }
        for(Stm s : elseBody){
            s.visit(visitor);
        }
        visitor.exitIf(this);
    }
}
