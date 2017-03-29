package intermediateModelHelper.heuristic.beta;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.ASTBinary;
import intermediateModel.visitors.DefualtASTREVisitor;

/**
 * Created by giovanni on 29/03/2017.
 */
public class Translation {

    public static void Translate(ASTRE re){
        IASTRE expr = re.getExpression();
        if(expr == null) return;
        expr.visit(new DefualtASTREVisitor(){
            @Override
            public void enterASTbinary(ASTBinary elm) {
                if(elm.getOp() == IASTRE.OPERATOR.minus){
                    IASTRE l = elm.getLeft();
                    IASTRE r = elm.getRight();
                    elm.setLeft(r);
                    elm.setRight(l);
                }
            }
        });

    }
}
