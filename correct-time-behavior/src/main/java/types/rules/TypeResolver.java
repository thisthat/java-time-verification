package types.rules;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.expression.*;
import types.definition.TimeType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TypeResolver {


    public static TimeType resolveTimerType(IASTRE expr) {
        if(expr instanceof ASTArrayInitializer)                 { return arrayInit((ASTArrayInitializer)expr); }
        else if(expr instanceof ASTAssignment)                  { return assignment((ASTAssignment)expr); }
        else if(expr instanceof ASTAttributeAccess)             { return attributeAccess((ASTAttributeAccess)expr); }
        else if(expr instanceof ASTBinary)                      { return binary((ASTBinary)expr); }
        else if(expr instanceof ASTCast)                        { return cast((ASTCast)expr); }
        else if(expr instanceof ASTConditional)                 { return ternary((ASTConditional)expr); }
        else if(expr instanceof ASTIdentifier)                  { return identifier((ASTIdentifier)expr); }
        else if(expr instanceof ASTLiteral)                     { return literal((ASTLiteral)expr); }
        else if(expr instanceof ASTMethodCall)                  { return call((ASTMethodCall)expr); }
        else if(expr instanceof ASTNewObject)                   { return newObject((ASTNewObject)expr); }
        else if(expr instanceof ASTPostOp)                      { return post((ASTPostOp)expr); }
        else if(expr instanceof ASTPreOp)                       { return pre((ASTPreOp)expr); }
        else if(expr instanceof ASTUnary)                       { return unary((ASTUnary)expr); }
        else if(expr instanceof ASTVariableDeclaration)         { return var((ASTVariableDeclaration)expr); }
        else if(expr instanceof ASTVariableMultipleDeclaration) { return var((ASTVariableMultipleDeclaration)expr); }
        else { return errorHandling(expr); }
    }

    private static TimeType errorHandling(IASTRE expr) {
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
        String msg = "Errors @ " + expr.getLine() + "--" + Arrays.stream(cause).map(s -> s + "\n").collect(Collectors.joining());
        throw new RuntimeException(msg);
    }

    private static TimeType var(ASTVariableMultipleDeclaration expr) {
        return errorHandling(expr);
    }

    private static TimeType var(ASTVariableDeclaration expr) {
        return errorHandling(expr);
    }

    private static TimeType unary(ASTUnary expr) {
        return errorHandling(expr);
    }

    private static TimeType pre(ASTPreOp expr) {
        return errorHandling(expr);
    }

    private static TimeType post(ASTPostOp expr) {
        return errorHandling(expr);
    }

    private static TimeType newObject(ASTNewObject expr) {
        return errorHandling(expr);
    }

    private static TimeType call(ASTMethodCall expr) {
        return errorHandling(expr);
    }

    private static TimeType literal(ASTLiteral expr) {
        return errorHandling(expr);
    }

    private static TimeType identifier(ASTIdentifier expr) {
        return errorHandling(expr);
    }

    private static TimeType ternary(ASTConditional expr) {
        return errorHandling(expr);
    }

    private static TimeType binary(ASTBinary expr) {
        return errorHandling(expr);
    }

    private static TimeType assignment(ASTAssignment expr) {
        return errorHandling(expr);
    }

    private static TimeType attributeAccess(ASTAttributeAccess expr) {
        return errorHandling(expr);
    }

    private static TimeType arrayInit(ASTArrayInitializer expr) {
        return errorHandling(expr);
    }

    private static TimeType cast(ASTCast expr) {
        return errorHandling(expr);
    }


}
