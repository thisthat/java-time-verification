package intermediateModel.types.rules;

import debugger.Debugger;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.expression.*;
import intermediateModel.types.definition.Duration;
import intermediateModel.types.definition.TimeType;
import intermediateModel.types.definition.Timestamp;
import intermediateModel.types.definition.Unknown;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.temporalTypes.TemporalTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypeResolver {

    static Debugger log = Debugger.getInstance(false);

    public static TimeType resolveTimerType(IASTRE expr, Env e) throws TimeException {
        log.log("Analysing line: " + expr.getLine());
        log.log("Analysing: " + expr.print());
        if (expr.isTimeCritical() || (expr instanceof ASTMethodCall && ((ASTMethodCall) expr).isTimeCall()) ||
                (expr instanceof ASTUnary && ((ASTUnary) expr).getExpr().isTimeCritical())
            )
            return resolveTimerTypeExpression(expr, e);
        return null;
    }

    private static TimeType resolveTimerTypeExpression(IASTRE expr, Env e) throws TimeException {
        if (expr instanceof ASTArrayInitializer) {
            return arrayInit((ASTArrayInitializer) expr, e);
        } else if (expr instanceof ASTAssignment) {
            return assignment((ASTAssignment) expr, e);
        } else if (expr instanceof ASTAttributeAccess) {
            return attributeAccess((ASTAttributeAccess) expr, e);
        } else if (expr instanceof ASTBinary) {
            return binary((ASTBinary) expr, e);
        } else if (expr instanceof ASTCast) {
            return cast((ASTCast) expr, e);
        } else if (expr instanceof ASTConditional) {
            return ternary((ASTConditional) expr, e);
        } else if (expr instanceof ASTIdentifier) {
            return identifier((ASTIdentifier) expr, e);
        } else if (expr instanceof ASTLiteral) {
            return literal((ASTLiteral) expr, e);
        } else if (expr instanceof ASTMethodCall) {
            return call((ASTMethodCall) expr, e);
        } else if (expr instanceof ASTNewObject) {
            return newObject((ASTNewObject) expr, e);
        } else if (expr instanceof ASTPostOp) {
            return post((ASTPostOp) expr, e);
        } else if (expr instanceof ASTPreOp) {
            return pre((ASTPreOp) expr, e);
        } else if (expr instanceof ASTUnary) {
            return unary((ASTUnary) expr, e);
        } else if (expr instanceof ASTVariableDeclaration) {
            return var((ASTVariableDeclaration) expr, e);
        } else if (expr instanceof ASTVariableMultipleDeclaration) {
            return var((ASTVariableMultipleDeclaration) expr, e);
        } else {
            return errorHandling(expr, e);
        }
    }


    private static TimeType errorHandling(IASTRE expr, Env e) {
        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
        String msg = "Errors @ " + expr.getLine() + " \n" + expr.print() + "\n-- \n" + Arrays.stream(cause).map(s -> s + "\n").collect(Collectors.joining());
        throw new RuntimeException(msg);
    }

    private static TimeType var(ASTVariableMultipleDeclaration expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType var(ASTVariableDeclaration expr, Env e) throws TimeException {
        return assignment(new ASTAssignment(expr.getStart(), expr.getEnd(),
                expr.getName(),
                expr.getExpr(),
                IASTRE.OPERATOR.equal), e);
    }

    private static TimeType unary(ASTUnary expr, Env e) throws TimeException {
        switch(expr.getOp()) {
            case not:
            case minus:
                return resolveTimerTypeExpression(expr.getExpr(), e);
        }
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

    private static TimeType call(ASTMethodCall expr, Env e) throws TimeException {
        if (expr.isMaxMin()) {
            IASTRE first = expr.getParameters().get(0);
            IASTRE second = expr.getParameters().get(1);
            TimeType left = resolveTimerTypeExpression(first, e);
            TimeType right = resolveTimerTypeExpression(second, e);
            if (left.equals(right)) {
                return left;
            }
            // unknown cases
            if (left instanceof Unknown && right instanceof Duration) {
                TimeType t = new Duration();
                setVariableUnknown(first, e, t);
                return t;
            }
            if (left instanceof Unknown && right instanceof Timestamp) {
                TimeType t = new Timestamp();
                setVariableUnknown(first, e, t);
                return t;
            }
            if (left instanceof Duration && right instanceof Unknown) {
                TimeType t = new Duration();
                setVariableUnknown(second, e, t);
                return t;
            }
            if (left instanceof Timestamp && right instanceof Unknown) {
                TimeType t = new Timestamp();
                setVariableUnknown(second, e, t);
                return t;
            }
            //all other cases are sure to be error
            throw new TimeTypeError(expr.getLine(), String.format("Not valid type for min/max operation! %s %s", left, right));
        } else if (expr.getTimeType() != null) {
            switch (expr.getTimeType()) {
                case RT_T: {
                        //does it still accept Timestamp time?
                        int[] timePars = TemporalTypes.getInstance().getTimeoutParametersET_T(expr);
                        for (int index : timePars) {
                            IASTRE v = expr.getParameters().get(index);
                            setVariableUnknown(v, e, new Timestamp());
                        }
                        //does it still accept Duration time?
                        timePars = TemporalTypes.getInstance().getTimeoutParametersET_D(expr);
                        for (int index : timePars) {
                            IASTRE v = expr.getParameters().get(index);
                            setVariableUnknown(v, e, new Duration());
                        }
                    }
                    //anyway, it returns timestamp
                    return new Timestamp();
                case RT_D: {
                        //does it still accept Timestamp time?
                        int[] timePars = TemporalTypes.getInstance().getTimeoutParametersET_T(expr);
                        for (int index : timePars) {
                            IASTRE v = expr.getParameters().get(index);
                            setVariableUnknown(v, e, new Timestamp());
                        }
                        //does it still accept Duration time?
                        timePars = TemporalTypes.getInstance().getTimeoutParametersET_D(expr);
                        for (int index : timePars) {
                            IASTRE v = expr.getParameters().get(index);
                            setVariableUnknown(v, e, new Duration());
                        }
                    }
                    //anyway, it returns duration
                    return new Duration();
                case ET_T:
                    for (IASTRE p : expr.getParameters()) {
                        setVariableUnknown(p, e, new Timestamp());
                    }
                    return null;
                case ET_D:
                    for (IASTRE p : expr.getParameters()) {
                        setVariableUnknown(p, e, new Duration());
                    }
                    return null;
            }
        }
        return new Unknown();
    }

    private static TimeType literal(ASTLiteral expr, Env e) throws TimeException {
        try {
            //Integer.parseInt(expr.getValue());
            Float.parseFloat(expr.getValue());
        } catch (Exception ex) {
            throw new TimeTypeError(expr.getLine(), "Cannot infer types of non integer scalars");
        }
        return new Duration();
    }

    private static TimeType identifier(ASTIdentifier expr, Env e) {
        IASTVar v = e.getVar(expr.getValue());
        if(v == null){
            return new Unknown();
        }
        TimeType t = v.getVarTimeType();
        if (t == null) {
            log.log(String.format("Variable %s @%d unknown time type", v.getName(), expr.getLine()));
            TimeType tvar = new Unknown();
            v.setVarTimeType(tvar);
            return tvar;
        }
        return t;
    }

    private static TimeType ternary(ASTConditional expr, Env e) throws TimeException {
        TimeType _then = resolveTimerTypeExpression(expr.getThenExpr(), e);
        TimeType _else = resolveTimerTypeExpression(expr.getElseExpr(), e);
        if (_then != null)
            return _then;
        else
            return _else;
    }

    private static TimeType binary(ASTBinary expr, Env e) throws TimeException {
        switch (expr.getOp()) {
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

    private static TimeType handleInt(ASTBinary expr, Env e) throws TimeException {
        TimeType left = resolveTimerTypeExpression(expr.getLeft(), e);
        TimeType right = resolveTimerTypeExpression(expr.getRight(), e);
        if(left == null){
            left = new Unknown();
        }
        if(right == null){
            right = new Unknown();
        }
        IASTRE.OPERATOR op = expr.getOp();
        if (op == IASTRE.OPERATOR.minus) {
            if (left.equals(right)) { //same type ok both T or D, return D.
                return new Duration();
            }
            if (left instanceof Timestamp && right instanceof Duration) {
                return new Timestamp();
            }
            //unknown cases
            if (left instanceof Timestamp && right instanceof Unknown) {
                return new Unknown();
            }
            if (left instanceof Duration && right instanceof Unknown) {
                TimeType t = new Duration();
                setVariableUnknown(expr.getRight(), e, t);
                return new Duration();
            }
            if (left instanceof Unknown && right instanceof Timestamp) {
                TimeType t = new Timestamp();
                setVariableUnknown(expr.getLeft(), e, t);
                return new Duration();
            }
            if (left instanceof Unknown && right instanceof Duration) {
                return new Unknown();
            }
            //all other cases are sure to be error
            throw new TimeTypeError(expr.getLine(), String.format("Not a valid operation! %s %s %s", left, op, right));

        } else if (op == IASTRE.OPERATOR.plus) {
            if (left instanceof Timestamp && right instanceof Duration) {
                return new Timestamp();
            }
            if (left instanceof Duration && right instanceof Duration) {
                return new Duration();
            }
            if (left instanceof Duration && right instanceof Timestamp) {
                return new Timestamp();
            }
            // Unknown cases
            if (left instanceof Timestamp && right instanceof Unknown) {
                TimeType t = new Duration();
                setVariableUnknown(expr.getRight(), e, t);
                return new Timestamp();
            }
            if (left instanceof Duration && right instanceof Unknown) {
                return new Unknown();
            }
            if (left instanceof Unknown && right instanceof Timestamp) {
                TimeType t = new Duration();
                setVariableUnknown(expr.getLeft(), e, t);
                return new Timestamp();
            }
            if (left instanceof Unknown && right instanceof Duration) {
                return new Unknown();
            }
            //all other cases are sure to be error
            throw new TimeTypeError(expr.getLine(), String.format("Not valid operation! %s %s %s", left, op, right));

        } else if (op == IASTRE.OPERATOR.mul) {
            if (left instanceof Duration && right instanceof Duration) {
                return new Duration();
            }
            if(left instanceof Unknown || right instanceof Unknown){
                throw new TimeTypeWarning(expr.getLine(), String.format("We cannot infer time types! Please check manually. %s %s %s", left, op, right));
            }

            //all other cases are sure to be error
            throw new TimeTypeError(expr.getLine(), String.format("Not valid operation! %s %s %s", left, op, right));
        } else if (op == IASTRE.OPERATOR.div) {
            if (left instanceof Duration && right instanceof Duration) {
                return new Duration();
            }
            if(left instanceof Unknown || right instanceof Unknown){
                throw new TimeTypeWarning(expr.getLine(), String.format("We cannot infer time types! Please check manually. %s %s %s", left, op, right));
            }
            //all other cases are sure to be error
            throw new TimeTypeError(expr.getLine(), String.format("Not valid operation! %s %s %s", left, op, right));
        }
        // we could either return an error or be conservative and say ok we don't know
        return new Unknown();
    }

    private static void setVariableUnknown(IASTRE right, Env e, TimeType t) {
        log.log(String.format("Resolving Unknown @%d with %s", right.getLine(), t));
        right.visit(new DefualtASTREVisitor() {
            @Override
            public void enterASTIdentifier(ASTIdentifier elm) {
                IASTVar v = e.getVar(elm.getValue());
                if (v != null) {
                    TimeType type = v.getVarTimeType();
                    if (type == null || type instanceof Unknown) {
                        v.setVarTimeType(t);
                        log.log("\t" + elm.getValue() + " has type " + t);
                    }
                }
            }
        });
    }

    private static TimeType handleBoolean(ASTBinary expr, Env e) throws TimeException {
        TimeType left = resolveTimerTypeExpression(expr.getLeft(), e);
        TimeType right = resolveTimerTypeExpression(expr.getRight(), e);
        if (!left.equals(right))
            throw new TimeTypeError(expr.getLine(), String.format("Boolean operation with not compatible intermediateModel.types. Left %s, Right %s", left, right));
        log.log(String.format("Boolean @%d : %s", expr.getLine(), left));
        return left;
    }

    private static TimeType assignment(ASTAssignment expr, Env e) throws TimeException {
        TimeType texpr = resolveTimerTypeExpression(expr.getRight(), e);
        List<String> ids = new ArrayList<>();
        expr.getLeft().visit(new DefualtASTREVisitor() {
            @Override
            public void enterASTAttributeAccess(ASTAttributeAccess elm) {
                ids.add(elm.getAttributeName());
            }

            @Override
            public void enterASTIdentifier(ASTIdentifier elm) {
                ids.add(elm.getValue());
            }
        });

        for(String id : ids) {
            IASTVar v = e.getVar(id);
            if(v == null)
                continue;
            TimeType tvar = v.getVarTimeType();
            if (tvar != null && !texpr.equals(tvar)) {
                throw new TimeTypeError(expr.getLine(), String.format("Variable %s change time type from %s to %s", v.getName(), tvar, texpr));
            }
            v.setVarTimeType(texpr);
            log.log(String.format("Assignment @%d : %s", expr.getLine(), texpr));
        }
        return texpr;
    }

    private static TimeType attributeAccess(ASTAttributeAccess expr, Env e) {
        IASTVar v = e.getVar(expr.getAttributeName());
        if (v != null) {
            return v.getVarTimeType();
        }
        return new Unknown();
    }

    private static TimeType arrayInit(ASTArrayInitializer expr, Env e) {
        return errorHandling(expr, e);
    }

    private static TimeType cast(ASTCast expr, Env e) throws TimeException {
        return resolveTimerTypeExpression(expr.getExpr(), e);
    }


}
