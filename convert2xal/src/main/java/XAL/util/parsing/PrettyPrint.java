package XAL.util.parsing;

import XAL.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.stream.Stream;

import static XAL.util.parsing.Exists.*;
import static XAL.util.parsing.GetObjects.*;

/**
 * The class exports method that help to prettify the construction of XAL elements
 * The main scope is to remove all the <i>stupid</i> fancy attributes to the names used by the grammar.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PrettyPrint {

    /**
     * Convert ugly names of states wrt its type
     *
     * @param str   The type of the object to make prettier
     * @return      An hence visualization of the character that is faboulousssss
     */
    public static String prettyPrintClassName(String str, ParserRuleContext ctx){
        str = str.substring(str.indexOf("$") + 1).replace("Context","");
        String out = str;
        switch(str){
            case "LocalVariableDeclaration":
                out = "DeclVar_" + prettyPrintLocalVariableDeclaration(ctx);
                break;
            case "call_":
                out += prettyPrintMethodInvocation(ctx);
                break;
            case "ReturnStatement":
                out = "Return_";
                out += prettyPrintReturnStatement(ctx);
                break;
            case "ExpressionStatement":
                out = processPrettyPrintExpressionStatement(ctx);
            default: break;

        }
        return out;
    }

    private static String processPrettyPrintExpressionStatement(ParserRuleContext ctx){
        String out = getStmtType(ctx);
        switch(out){
            case "ExpressionStatementContext":
                if(Exists.hasMethodCall(ctx)){
                    out = "call_" + getLastMethodCall( ctx );
                }
                else if(Exists.hasNewObject(ctx)){
                    out = "new_" + getNewType(ctx);
                }
                break;
            default: break;
        }
        return out;
    }

    /**
     * Resolve the name of the identifier of the variable.
     *
     * @param ctx   The tree element in which search the variable identificator.
     * @return      The variable name.
     */
    public static String prettyPrintLocalVariableDeclaration(ParserRuleContext ctx){
        String type = null;
        if(ctx instanceof VariableDeclaratorIdContext){
            type = ctx.getText();
        } else {
            for (ParseTree elm : ctx.children ) {
                if(elm instanceof TerminalNode)
                    continue;
                String tmp = prettyPrintLocalVariableDeclaration((ParserRuleContext) elm);
                if(type == null){
                    type = tmp;
                }
            }
        }
        return type;
    }

    /**
     * Resolve the name of the method called. It takes the first one if are concatenated.
     *
     * @param ctx   The tree element to visit to get the method name.
     * @return      The method name.
     */
    public static String prettyPrintMethodInvocation(ParserRuleContext ctx){
        String type = null;
        if(ctx instanceof MethodInvocationContext){
            type = ctx.getChild(2).getText();
        } else {
            for (ParseTree elm : ctx.children ) {
                if(elm instanceof TerminalNode)
                    continue;
                String tmp = prettyPrintMethodInvocation((ParserRuleContext) elm);
                if(type == null){
                    type = tmp;
                }
            }
        }
        return type;
    }

    /**
     * Return the type of the return statement.
     *
     * @param ctx   The tree element to visit.
     * @return      The type of the return element.
     */
    public static String prettyPrintReturnStatement(ParserRuleContext ctx){
        return prettyPrintExpression(ctx);
    }

    /**
     * Prettify the expression with their type.
     * It takes the first expression that it finds in the tree.
     *
     * @TODO support more types
     * @param ctx   The tree element that contains the expression.
     * @return      The type of the expression.
     */
    public static String prettyPrintExpression(ParserRuleContext ctx){
        ExpressionContext expr = GetObjects.getExpression(ctx);
        String out = "nd";
        Pair<String,ParseTree> e = getExprTypeWithContext(expr);
        String expType = e.getFirst();
        expType = expType.substring(expType.indexOf("$") + 1).replace("Context","");
        switch (expType){
            case "MethodInvocation_lfno_primary":
                out = "call_" + e.getSecond().getChild(2).getText();
                break;
            case "PrimaryNoNewArray_lfno_primary":
            case "Primary":
                if(hasMethodCall((ParserRuleContext) e.getSecond())){
                    out = "call_" + getLastMethodCall((ParserRuleContext) e.getSecond());
                }
                break;
            case "Literal":
                out = escapeChars(e.getSecond().getText());
                break;
            case "ExpressionName":
                out = escapeChars(e.getSecond().getText());
                break;
            case "UnaryExpressionNotPlusMinus" :
                out = "not_";
                ParserRuleContext tmp = (ParserRuleContext)e.getSecond().getChild(1);
                if(hasMethodCall(tmp)) {
                    out += getLastMethodCall(tmp);
                }
                else {
                    out += prettyPrintExpression(tmp);
                }
            default:
                break;
        }
        return out;
    }


    protected static String escapeChars(String in){
        String out;
        out = in.replace("\"","").replace(" ","_").replace(".","_");
        return out;
    }
}
