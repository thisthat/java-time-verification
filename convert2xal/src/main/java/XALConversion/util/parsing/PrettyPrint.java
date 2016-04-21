package XALConversion.util.parsing;

import XALConversion.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import parser.grammar.Java8CommentSupportedParser.*;

import static XALConversion.util.parsing.Exists.*;
import static XALConversion.util.parsing.GetObjects.*;
import org.apache.commons.lang3.StringEscapeUtils.*;

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
            case "ReturnStatement":
                out = "Return_";
                out += ReturnStatement(ctx);
                break;
            case "ExpressionStatement":
                out = ExpressionStatement(ctx);
            case "MethodInvocation":
                out = "call_" + MethodInvocation(ctx);
            default: break;

        }
        return out;
    }

    private static String ExpressionStatement(ParserRuleContext ctx){
        String out = getStmtType(ctx);
        switch(out){
            case "ExpressionStatementContext":
                if(Exists.MethodCall(ctx)){
                    out = "call_" + getLastMethodCall( ctx );
                }
                else if(Exists.NewObject(ctx)){
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
        ExpressionContext expr = GetObjects.getExpression(ctx);
        String out = "nd_expr";
        Pair<String,ParseTree> e = getExprTypeWithContext(expr);
        String expType = e.getFirst();
        expType = expType.substring(expType.indexOf("$") + 1).replace("Context","");
        switch (expType){
            case "MethodInvocation_lfno_primary":
                out = "call_" + e.getSecond().getChild(2).getText();
                break;
            case "PrimaryNoNewArray_lfno_primary":
            case "Primary":
                if(MethodCall((ParserRuleContext) e.getSecond())){
                    out = "call_" + getLastMethodCall((ParserRuleContext) e.getSecond());
                }
                break;
            case "ExpressionName":
            case "Literal":
                out = escapeChars(e.getSecond().getText());
                break;
            case "RelationalExpression":
                out = escapeChars(e.getSecond().getChild(1).getText());
                break;
            case "UnaryExpressionNotPlusMinus" :
                out = "not_";
                ParserRuleContext tmp = (ParserRuleContext)e.getSecond().getChild(1);
                if(MethodCall(tmp)) {
                    out += getLastMethodCall(tmp);
                }
                else {
                    out += Expression(tmp);
                }
            default:
                break;
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
                new Pair<String,String>("<","lt")
        ));
        String out = in;
        for(Pair<String,String> p : filters){
            out = out.replace(p.getFirst() , p.getSecond());
        }
        return out;
    }
}
