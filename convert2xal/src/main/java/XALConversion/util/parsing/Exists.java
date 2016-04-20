package XALConversion.util.parsing;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import parser.grammar.Java8CommentSupportedParser.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Exists {

    /**
     * Check if a method call has a method call inside
     * @param method The method call
     * @return true if it has a method call as parameter, false otherwise
     */
    public static boolean existsMethodInvocationInsideMethodCall(ParserRuleContext method){
        boolean flag = false;
        for (ParseTree elm: method.children ) {
            if (
                    elm instanceof MethodInvocation_lf_primaryContext ||
                    elm instanceof MethodInvocation_lfno_primaryContext ||
                    elm instanceof MethodInvocationContext
                ) {
                return true;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || existsMethodInvocationInsideMethodCall((ParserRuleContext) elm);
            }
        }
        return flag;
    }


    /**
     * Check if there is if inside the node element
     *
     * @param ctx   The node element from where start the scan.
     * @return      True if there is an if node inside.
     */
    public static boolean If(ParserRuleContext ctx) {
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof IfThenElseStatementContext ||
                            c instanceof IfThenElseStatementNoShortIfContext ||
                            c instanceof IfThenStatementContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | If((ParserRuleContext) c);
            }
        }
        return f;
    }

    /**
     * Check if there is if inside the node element
     *
     * @param ctx   The node element from where start the scan.
     * @return      True if there is an if node inside.
     */
    public static boolean For(ParserRuleContext ctx) {
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof BasicForStatementContext
                )
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | For((ParserRuleContext) c);
            }
        }
        return f;
    }

    public static boolean Has2Walk(ParserRuleContext ctx){
        return If(ctx) || For(ctx);
    }

    /**
     * Check if there is a method call inside the element.
     * @param ctx   The node from where start to scan.
     * @return      True if a method is called.
     */
    public static boolean hasMethodCall(ParserRuleContext ctx){
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof MethodInvocationContext ||
                    c instanceof MethodInvocation_lfno_primaryContext ||
                    c instanceof MethodInvocation_lf_primaryContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | hasMethodCall((ParserRuleContext) c);
            }
        }
        return f;
    }

    public static boolean hasNewObject(ParserRuleContext ctx){
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (c instanceof ClassInstanceCreationExpression_lfno_primaryContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | hasNewObject((ParserRuleContext) c);
            }
        }
        return f;
    }

    /**
     * Check if there is an expression inside the node.
     * @param node  The point from where start to scan.
     * @return      True if an expression is founded.
     */
    public static boolean hasExpression(ParserRuleContext node){
        boolean flag = false;
        for (ParseTree elm: node.children ) {
            if ( elm instanceof ExpressionContext) {
                return true;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || hasExpression((ParserRuleContext) elm);
            }
        }
        return flag;
    }
}
