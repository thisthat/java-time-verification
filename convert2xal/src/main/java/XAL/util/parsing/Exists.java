package XAL.util.parsing;

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

    public static boolean isIf(ParserRuleContext ctx) {
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
                f = false;
            }
            else {
                f = f | isIf((ParserRuleContext) c);
            }
        }
        return f;
    }

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
                f = false;
            }
            else {
                f = f | hasMethodCall((ParserRuleContext) c);
            }
        }
        return f;
    }

    /**
     *
     *
     * @param node
     * @return
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
