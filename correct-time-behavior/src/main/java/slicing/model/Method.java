package slicing.model;

import slicing.model.interfaces.Stm;
import slicing.model.visitor.ReducedVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 11/07/2017.
 */
public class Method extends Stm {

    List<Stm> body = new ArrayList<>();
    String name;
    List<String> signature;

    public Method(int start, int end, int line, int lineEnd, String code) {
        super(start, end, line, lineEnd, code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSignature() {
        return signature;
    }

    public void setSignature(List<String> signature) {
        this.signature = signature;
    }

    public List<Stm> getBody() {
        return body;
    }

    public void setBody(List<Stm> body) {
        this.body = body;
    }

    @Override
    public void visit(ReducedVisitor visitor) {
        for(Stm s : body){
            s.visit(visitor);
        }
    }
}
