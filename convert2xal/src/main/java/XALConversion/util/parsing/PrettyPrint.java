package XALConversion.util.parsing;

import XALConversion.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.grammar.Java8CommentSupportedParser.*;

import XALConversion.util.parsing.Exists.*;
import XALConversion.util.parsing.GetObjects.*;

import java.util.*;

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
    public static String ClassName(String str, ParserRuleContext ctx){
        str = str.substring(str.indexOf("$") + 1).replace("Context","");
        String out = str;
        switch(str){
            case "LocalVariableDeclaration":
                out = "DeclVar_" + LocalVariableDeclaration(ctx);
                break;
            case "call_":
                out += MethodInvocation(ctx);
                break;
            case "MethodInvocation":
                out = "call_" + MethodInvocation(ctx);
                break;
            case "ReturnStatement":
                out = "Return_";
                out += ReturnStatement(ctx);
                break;
            case "Assignment":
            case "ExpressionStatement":
                out = ExpressionStatement(str,ctx);
                break;
            case "PostIncrementExpression":
            case "PreIncrementExpression":
                out = "inc_" + PrePostExpression(ctx);
                break;
            case "PostDecrementExpression":
            case "PreDecrementExpression":
                out = "dec_" + PrePostExpression(ctx);
                break;
            default: break;

        }
        return out;
    }

    private static String ExpressionStatement(String str, ParserRuleContext ctx){
        String out = str;
        switch(out){
            case "ExpressionContext":
            case "ExpressionStatement":
                if(Exists.MethodCall(ctx)){
                    out = "call_" + GetObjects.getLastMethodCall( ctx );
                }
                else if(Exists.NewObject(ctx)){
                    out = "new_" + GetObjects.getNewType(ctx);
                }
                else {
                    out = Expression(ctx);
                }
                break;
            case "Assignment":
                AssignmentContext asg = GetObjects.Assignment(ctx);
                out =  asg.getChild(0).getText() + "_takes_" +
                        ExpressionStatement(
                                asg.getChild(2).getClass().getSimpleName(),
                                (ParserRuleContext) asg.getChild(2)
                        );
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
    public static String LocalVariableDeclaration(ParserRuleContext ctx){
        String type = null;
        if(ctx instanceof VariableDeclaratorIdContext){
            type = ctx.getText();
        } else {
            for (ParseTree elm : ctx.children ) {
                if(elm instanceof TerminalNode)
                    continue;
                String tmp = LocalVariableDeclaration((ParserRuleContext) elm);
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
    public static String MethodInvocation(ParserRuleContext ctx){
        String type = null;
        if(ctx instanceof MethodInvocationContext){
            String prefix = "";
            if(ctx.getChild(0) instanceof TypeNameContext){
                prefix = TypeName((TypeNameContext) ctx.getChild(0));
            }
            type = prefix + ctx.getChild(2).getText();
        } else {
            for (ParseTree elm : ctx.children ) {
                if(elm instanceof TerminalNode)
                    continue;
                String tmp = MethodInvocation((ParserRuleContext) elm);
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
    public static String ReturnStatement(ParserRuleContext ctx){
        return Expression(ctx);
    }

    /**
     * Prettify the expression with their type.
     * It takes the first expression that it finds in the tree.
     *
     * @TODO support more types
     * @param ctx   The tree element that contains the expression.
     * @return      The type of the expression.
     */
    public static String Expression(ParserRuleContext ctx){
        String out = "nd_expr";
        ExpressionContext expr = GetObjects.getExpression(ctx);
        Pair<String,ParseTree> e = GetObjects.getExprTypeWithContext(expr);
        String expType = e.getFirst();
        expType = expType.substring(expType.indexOf("$") + 1).replace("Context","");
        switch (expType){
            case "MethodInvocation_lfno_primary":
                out = "call_" + e.getSecond().getChild(2).getText();
                break;
            case "PrimaryNoNewArray_lfno_primary":
            case "Primary":
                if(Exists.MethodCall((ParserRuleContext) e.getSecond())){
                    out = "call_" + GetObjects.getLastMethodCall((ParserRuleContext) e.getSecond());
                }
                break;
            case "ExpressionName":
            case "Literal":
                out = escapeChars(e.getSecond().getText());
                break;
            case "EqualityExpression":
            case "RelationalExpression":
                out = escapeChars(e.getSecond().getChild(1).getText());
                break;
            case "UnaryExpressionNotPlusMinus" :
                out = "not_";
                ParserRuleContext tmp = (ParserRuleContext)e.getSecond().getChild(1);
                if(Exists.MethodCall(tmp)) {
                    out += GetObjects.getLastMethodCall(tmp);
                }
                else {
                    out += Expression(tmp);
                }
            default:
                break;
        }
        return out;
    }

    private static String PrePostExpression(ParserRuleContext ctx) {
        String out = "nd_PrePostExpr";
        ParserRuleContext exp = GetObjects.PrePostExpression(ctx);
        if(exp instanceof PostIncrementExpressionContext || exp instanceof PostDecrementExpressionContext){
            out = exp.getChild(0).getText();
        }
        else if(exp instanceof PreIncrementExpressionContext || exp instanceof PreDecrementExpressionContext){
            out = exp.getChild(1).getText();
        }
        return out;
    }

    public static String TypeName(TypeNameContext tn){
        return escapeChars(tn.getText() + ".");
    }

    protected static String escapeChars(String in){
        final Set<Pair<String,String>> filters = new HashSet<Pair<String,String>>(Arrays.asList(
                new Pair<String,String>("\"",""),
                new Pair<String,String>(" ","_"),
                new Pair<String,String>(".","_"),
                new Pair<String,String>("<","_ltExpr_"),
                new Pair<String,String>("<=","_lteExpr_"),
                new Pair<String,String>(">","_gtExpr_"),
                new Pair<String,String>(">=","_gteExpr_"),
                new Pair<String,String>("==","_eqExpr_")
        ));
        String out = in;
        for(Pair<String,String> p : filters){
            out = out.replace(p.getFirst() , p.getSecond());
        }
        return out;
    }
}
