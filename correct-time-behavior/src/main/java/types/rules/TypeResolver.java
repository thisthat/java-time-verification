package types.rules;

import debugger.Debugger;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.expression.*;
import intermediateModel.typedefinition.Duration;
import intermediateModel.typedefinition.TimeType;
import intermediateModel.typedefinition.Timestamp;
import intermediateModel.typedefinition.Unknown;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TypeResolver {

   static Debugger log = Debugger.getInstance(false);

    public static TimeType resolveTimerType(IASTRE expr, Env e) {
        if(!expr.isTimeCritical())
            return null;
        return resolveTimerTypeExpression(expr, e);
    }

    private static TimeType resolveTimerTypeExpression(IASTRE expr, Env e) {
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
        return assignment(new ASTAssignment(expr.getStart(), expr.getEnd(),
                expr.getName(),
                expr.getExpr(),
                IASTRE.OPERATOR.equal), e);
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
        if(expr.isMaxMin()){
            IASTRE first = expr.getParameters().get(0);
            IASTRE second = expr.getParameters().get(1);
            TimeType left = resolveTimerTypeExpression(first, e);
            TimeType right = resolveTimerTypeExpression(second, e);
            if(left.equals(right)){
                return left;
            }
            // unknown cases
            if(left instanceof Unknown && right instanceof Duration){
                TimeType t = new Duration();
                setVariableUnknown(first, e, t);
                return t;
            }
            if(left instanceof Unknown && right instanceof Timestamp){
                TimeType t = new Timestamp();
                setVariableUnknown(first, e, t);
                return t;
            }
            if(left instanceof Duration && right instanceof Unknown){
                TimeType t = new Duration();
                setVariableUnknown(second, e, t);
                return t;
            }
            if(left instanceof Timestamp && right instanceof Unknown){
                TimeType t = new Timestamp();
                setVariableUnknown(second, e, t);
                return t;
            }
            //all other cases are sure to be error
            throw new RuntimeException(String.format("Not valid type for min/max operation @%d! %s %s",
                    expr.getLine(), left, right));
        } else if(expr.getTimeType() != null) {
            switch (expr.getTimeType()) {
                case RT_T:
                    return new Timestamp();
                case RT_D:
                    return new Duration();
            }
        }
        return errorHandling(expr, e);
    }

    private static TimeType literal(ASTLiteral expr, Env e) {
        try {
            Integer.parseInt(expr.getValue());
        } catch (Exception ex){
            throw new RuntimeException("Cannot decide time types of not integers @" + expr.getLine());
        }
        return new Duration();
    }

    private static TimeType identifier(ASTIdentifier expr, Env e) {
        IASTVar v = e.getVar(expr.getValue());
        TimeType t = v.getVarTimeType();
        if(t == null){
            log.log(String.format("Variable %s @%d unknown time type", v.getName(), expr.getLine()));
            TimeType tvar = new Unknown();
            v.setVarTimeType(tvar);
            return tvar;
        }
        return t;
    }

    private static TimeType ternary(ASTConditional expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType binary(ASTBinary expr, Env e) {
        switch (expr.getOp()){
            case or:
            case and:
            case not:
            case notEqual:
            case less:
            case lessEqual:
            case greater:
            case greaterEqual:
            case equality:
                return handleBoolean(expr, e);
            case plus:
            case div:
            case mod:
            case minus:
            case mul:
                return handleInt(expr, e);
            default:
                throw new RuntimeException("Operator not covered! OP: " + expr.getOp());
        }
    }

    private static TimeType handleInt(ASTBinary expr, Env e) {
        TimeType left = resolveTimerTypeExpression(expr.getLeft(), e);
        TimeType right = resolveTimerTypeExpression(expr.getRight(), e);
        IASTRE.OPERATOR op = expr.getOp();
        if(op == IASTRE.OPERATOR.minus){
            if(left.equals(right)){ //same type ok both T or D, return D.
                return new Duration();
            }
            if(left instanceof Timestamp && right instanceof Duration){
                return new Timestamp();
            }
            //unknown cases
            if(left instanceof Timestamp && right instanceof Unknown){
                return new Unknown();
            }
            if(left instanceof Duration && right instanceof Unknown){
                TimeType t = new Duration();
                setVariableUnknown(expr.getRight(), e, t);
                return new Duration();
            }
            if(left instanceof Unknown && right instanceof Timestamp){
                TimeType t = new Timestamp();
                setVariableUnknown(expr.getLeft(), e, t);
                return new Duration();
            }
            if(left instanceof Unknown && right instanceof Duration){
                return new Unknown();
            }
            //all other cases are sure to be error
            throw new RuntimeException(String.format("Not valid operation @%d! %s %s %s",
                    expr.getLine(), left, op, right));

        } else if(op == IASTRE.OPERATOR.plus){
            if(left instanceof Timestamp && right instanceof Duration){
                return new Timestamp();
            }
            if(left instanceof Duration && right instanceof Duration){
                return new Duration();
            }
            if(left instanceof Duration && right instanceof Timestamp){
                return new Timestamp();
            }
            // Unknown cases
            if(left instanceof Timestamp && right instanceof Unknown){
                TimeType t = new Duration();
                setVariableUnknown(expr.getRight(), e, t);
                return new Timestamp();
            }
            if(left instanceof Duration && right instanceof Unknown){
                return new Unknown();
            }
            if(left instanceof Unknown && right instanceof Timestamp){
                TimeType t = new Duration();
                setVariableUnknown(expr.getLeft(), e, t);
                return new Timestamp();
            }
            if(left instanceof Unknown && right instanceof Duration){
                return new Unknown();
            }
            //all other cases are sure to be error
            throw new RuntimeException(String.format("Not valid operation @%d! %s %s %s",
                    expr.getLine(), left, op, right));

        } else if(op == IASTRE.OPERATOR.mul){
            if(left instanceof Duration && right instanceof Duration){
                return new Duration();
            }
            //all other cases are sure to be error
            throw new RuntimeException(String.format("Not valid operation @%d! %s %s %s",
                    expr.getLine(), left, op, right));
        } else if(op == IASTRE.OPERATOR.div){
            if(left instanceof Duration && right instanceof Duration){
                return new Duration();
            }
            //all other cases are sure to be error
            throw new RuntimeException(String.format("Not valid operation @%d! %s %s %s",
                    expr.getLine(), left, op, right));
        }
        // we could either return an error or be conservative and say ok we don't know
        return new Unknown();
    }

    private static void setVariableUnknown(IASTRE right, Env e, TimeType t) {
        log.log(String.format("Resolving Unknown @%d with %s", right.getLine(), t));
        right.visit(new DefualtASTREVisitor(){
            @Override
            public void enterASTIdentifier(ASTIdentifier elm) {
                IASTVar v = e.getVar(elm.getValue());
                TimeType type = v.getVarTimeType();
                if(type == null || type instanceof Unknown) {
                    v.setVarTimeType(t);
                    log.log("\t" + elm.getValue() + " has type " + t);
                }
            }
        });
    }

    private static TimeType handleBoolean(ASTBinary expr, Env e) {
        TimeType left = resolveTimerTypeExpression(expr.getLeft(), e);
        TimeType right = resolveTimerTypeExpression(expr.getRight(), e);
        if(!left.equals(right))
            throw new RuntimeException(String.format("Boolean operation @%d not compatible types. Left %s, Right %s", expr.getLine(), left, right));
        log.log(String.format("Boolean @%d : %s", expr.getLine(), left));
        return left;
    }

    private static TimeType assignment(ASTAssignment expr, Env e) {
        TimeType texpr = resolveTimerTypeExpression(expr.getRight(), e);
        IASTVar v = e.getVar(expr.getLeft().print());
        TimeType tvar = v.getVarTimeType();
        if(tvar != null && !texpr.equals(tvar)){
            throw new RuntimeException(String.format("Variable %s change time type from %s to %s", v.getName(), tvar, texpr));
        }
        v.setVarTimeType(texpr);
        log.log(String.format("Assignment @%d : %s", expr.getLine(), texpr));
        return texpr;
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
