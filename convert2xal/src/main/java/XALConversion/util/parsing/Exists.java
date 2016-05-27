package XALConversion.util.parsing;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import parser.grammar.Java8CommentSupportedParser;
import parser.grammar.Java8CommentSupportedParser.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Exists {

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
     * Check if there is For inside the node element
     *
     * @param ctx   The node element from where start the scan.
     * @return      True if there is an if node inside.
     */
    public static boolean For(ParserRuleContext ctx) {
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof BasicForStatementContext ||
                    c instanceof EnhancedForStatementContext
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

    /**
     * Check if there is While inside the node element
     *
     * @param ctx   The node element from where start the scan.
     * @return      True if there is an while node inside.
     */
    public static boolean While(ParserRuleContext ctx) {
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof WhileStatementContext ||
                    c instanceof WhileStatementNoShortIfContext
                    )
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | While((ParserRuleContext) c);
            }
        }
        return f;
    }

    /**
     * Check if there is Synchronized inside the node element
     *
     * @param ctx   The node element from where start the scan.
     * @return      True if there is an synchronized node inside.
     */
    public static boolean Synchronized(ParserRuleContext ctx){

        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (c instanceof SynchronizedStatementContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | Synchronized((ParserRuleContext) c);
            }
        }
        return f;
    }


    public static boolean Try(ParserRuleContext ctx){

        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (c instanceof TryStatementContext || c instanceof TryWithResourcesStatementContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | Try((ParserRuleContext) c);
            }
        }
        return f;
    }

    public static boolean Switch(ParserRuleContext ctx){

        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (c instanceof SwitchBlockContext || c instanceof SwitchStatementContext)
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | Switch((ParserRuleContext) c);
            }
        }
        return f;
    }


    public static boolean Has2Walk(ParserRuleContext ctx){
        return If(ctx) || For(ctx) || While(ctx) || Synchronized(ctx) || Try(ctx) || Switch(ctx);
    }

    /**
     * Check if there is a method call inside the element.
     * @param ctx   The node from where start to scan.
     * @return      True if a method is called.
     */
    public static boolean MethodCall(ParserRuleContext ctx){
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
                f = f | MethodCall((ParserRuleContext) c);
            }
        }
        return f;
    }

    public static boolean Block(ParserRuleContext ctx){
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (
                    c instanceof BlockStatementContext ||
                    c instanceof BlockStatementsContext
                )
            {
                f = true;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                f = f | Block((ParserRuleContext) c);
            }
        }
        return f;
    }

    public static boolean NewObject(ParserRuleContext ctx){
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
                f = f | NewObject((ParserRuleContext) c);
            }
        }
        return f;
    }

    /**
     * Check if there is an expression inside the node.
     * @param node  The point from where start to scan.
     * @return      True if an expression is founded.
     */
    public static boolean Expression(ParserRuleContext node){
        boolean flag = false;
        for (ParseTree elm: node.children ) {
            if ( elm instanceof ExpressionContext) {
                return true;
            }
            else if(elm instanceof TerminalNodeImpl){
                continue;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || Expression((ParserRuleContext) elm);
            }
        }
        return flag;
    }

    public static boolean Return(ParserRuleContext ctx) {
        boolean flag = false;
        for (ParseTree elm: ctx.children ) {
            if ( elm instanceof ReturnStatementContext) {
                return true;
            }
            else if(elm instanceof TerminalNodeImpl){
                continue;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || Return((ParserRuleContext) elm);
            }
        }
        return flag;
    }

	public static boolean Continue(ParserRuleContext ctx) {
		boolean flag = false;
		for (ParseTree elm: ctx.children ) {
			if ( elm instanceof ContinueStatementContext) {
				return true;
			}
			else if(elm instanceof TerminalNodeImpl){
				continue;
			}
			else {
				if(elm.getChildCount() > 0)
					flag = flag || Continue((ParserRuleContext) elm);
			}
		}
		return flag;
	}

	public static boolean Break(ParserRuleContext ctx) {
		boolean flag = false;
		for (ParseTree elm: ctx.children ) {
			if ( elm instanceof BreakStatementContext) {
				return true;
			}
			else if(elm instanceof TerminalNodeImpl){
				continue;
			}
			else {
				if(elm.getChildCount() > 0)
					flag = flag || Break((ParserRuleContext) elm);
			}
		}
		return flag;
	}

    public static boolean TryWithResource(ParserRuleContext ctx) {
        boolean flag = false;
        for (ParseTree elm: ctx.children ) {
            if ( elm instanceof TryWithResourcesStatementContext) {
                return true;
            }
            else if(elm instanceof TerminalNodeImpl){
                continue;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || TryWithResource((ParserRuleContext) elm);
            }
        }
        return flag;
    }

    public static boolean Throws(ParserRuleContext ctx) {
        boolean flag = false;
        for (ParseTree elm: ctx.children ) {
            if ( elm instanceof ThrowStatementContext) {
                return true;
            }
            else if(elm instanceof TerminalNodeImpl){
                continue;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || Throws((ParserRuleContext) elm);
            }
        }
        return flag;
    }
}
