package slicing.model.visitor;

import intermediateModel.interfaces.ASTREVisitor;
import slicing.model.Assignment;
import slicing.model.If;
import slicing.model.MethodCall;
import slicing.model.While;

/**
 * Created by giovanni on 11/07/2017.
 */
public interface ReducedVisitor extends ASTREVisitor {
    void enterAssignmet(Assignment elm);
    void enterIf(If elm);
    void enterMethodCall(MethodCall elm);
    void enterWhile(While elm);
    void exitAssignment(Assignment elm);
    void exitIf(If elm);
    void exitMethodCall(MethodCall elm);
    void exitWhile(While elm);

}
