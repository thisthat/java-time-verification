package types.rules;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.expression.*;
import intermediateModelHelper.envirorment.Env;
import types.definition.Duration;
import types.definition.TimeType;
import types.definition.Timestamp;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TypeResolver {


    public static TimeType resolveTimerType(IASTRE expr, Env e) {
        if(expr instanceof ASTArrayInitializer)                 { return arrayInit((ASTArrayInitializer)expr, e); }
        else if(expr instanceof ASTAssignment)                  { return assignment((ASTAssignment)expr, e); }
        else if(expr instanceof ASTAttributeAccess)             { return attributeAccess((ASTAttributeAccess)expr, e); }
        else if(expr instanceof ASTBinary)                      { return binary((ASTBinary)expr, e); }
        else if(expr instanceof ASTCast)                        { return cast((ASTCast)expr, e); }
        else if(expr instanceof ASTConditional)                 { return ternary((ASTConditional)expr, e); }
        else if(expr instanceof ASTIdentifier)                  { return identifier((ASTIdentifier)expr, e); }
        else if(expr instanceof ASTLiteral)                     { return literal((ASTLiteral)expr, e); }
        else if(expr instanceof ASTMethodCall)                  { return call((ASTMethodCall)expr, e); }
        else if(expr instanceof ASTNewObject)                   { return newObject((ASTNewObject)expr, e); }
        else if(expr instanceof ASTPostOp)                      { return post((ASTPostOp)expr, e); }
        else if(expr instanceof ASTPreOp)                       { return pre((ASTPreOp)expr, e); }
        else if(expr instanceof ASTUnary)                       { return unary((ASTUnary)expr, e); }
        else if(expr instanceof ASTVariableDeclaration)         { return var((ASTVariableDeclaration)expr, e); }
        else if(expr instanceof ASTVariableMultipleDeclaration) { return var((ASTVariableMultipleDeclaration)expr, e); }
        else { return errorHandling(expr, e); }
    }

    private static TimeType errorHandling(IASTRE expr, Env e) {
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
        String msg = "Errors @ " + expr.getLine() + "--" + Arrays.stream(cause).map(s -> s + "\n").collect(Collectors.joining());
        throw new RuntimeException(msg);
    }

    private static TimeType var(ASTVariableMultipleDeclaration expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType var(ASTVariableDeclaration expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType unary(ASTUnary expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType pre(ASTPreOp expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType post(ASTPostOp expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType newObject(ASTNewObject expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType call(ASTMethodCall expr, Env e) {
        switch (expr.getTimeType()){
            case RT_T: return new Timestamp();
            case RT_D: return new Duration();
        }
        return errorHandling(expr, e);
    }

    private static TimeType literal(ASTLiteral expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType identifier(ASTIdentifier expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType ternary(ASTConditional expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType binary(ASTBinary expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType assignment(ASTAssignment expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType attributeAccess(ASTAttributeAccess expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType arrayInit(ASTArrayInitializer expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType cast(ASTCast expr, Env e) {
        return errorHandling(expr, e);
    }


}
