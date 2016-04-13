package XAL.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.*;

/**
 * The class exports method that help the parsing of a source file in order to create easily a XAL document
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ParsingUtility {

    /**
     * Check if a method call has a method call inside
     * @param method The method call
     * @return true if it has a method call as parameter, false otherwise
     */
    public static boolean existsMethodInvocationInsideMethodCall(ParserRuleContext method){
        boolean flag = false;
        for (ParseTree elm: method.children ) {
            if (
                elm instanceof MethodInvocation_lf_primaryContext   ||
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
     * Collect all the variables and their types from a method definition.
     * @param ctx The list of parameters
     * @return A list of {@link Pair} contain &lt;Name, Type&gt;
     */
    public static List<Pair<String,String>> getParameterList(FormalParameterListContext ctx){
        //FormalParameterContext c = (FormalParameterContext) ctx.getChild(0);
        List<Pair<String,String>> ret = new ArrayList<Pair<String,String>>();
        if(ctx.getChild(0) instanceof LastFormalParameterContext){
            //handle the ... case
            if(ctx.getChild(0).getChild(0) instanceof FormalParameterContext){
                FormalParameterContext par = (FormalParameterContext) ctx.getChild(0).getChild(0);
                ret.add(new Pair<String,String>(getParameterName(par), getParameterType(par)));
            }
            else if (ctx.getChild(0).getChild(0) instanceof ThreeDotParameterContext){
                ThreeDotParameterContext tdp = (ThreeDotParameterContext) ctx.getChild(0).getChild(0);
                ret.add(new Pair<String,String>(tdp.getChild(2).getText(), tdp.getChild(0).getText() + "..."));
            }
            else {
                //for the later usage
            }
        }
        else {
            FormalParametersContext listPars = (FormalParametersContext) ctx.getChild(0);
            for(int i = 0; i < listPars.getChildCount(); i++){
                if(listPars.getChild(i) instanceof TerminalNode){
                    continue;
                }
                FormalParameterContext par = (FormalParameterContext) listPars.getChild(i);
                ret.add(new Pair<String,String>(getParameterName(par), getParameterType(par)));
            }
            //Skip second that is a comma
            FormalParameterContext par = (FormalParameterContext) ctx.getChild(2).getChild(0);
            ret.add(new Pair<String,String>(getParameterName(par), getParameterType(par)));
        }
        return ret;
    }

    /**
     * From a parameter, extract its name
     * @param par the parameter in object
     * @return its name
     */
    public static String getParameterName(FormalParameterContext par) {
        //System.out.println(par.getChild(1).getText());
        return par.getChild(1).getText();
    }

    /**
     * Extract the type of a parameter
     * @param par the parameter in object
     * @return its type
     */
    public static String getParameterType(FormalParameterContext par){
        return par.getChild(0).getText();
    }

    public static boolean hasExpression(ParserRuleContext node){
        boolean flag = false;
        for (ParseTree elm: node.children ) {
            if ( elm instanceof ExpressionContext ) {
                return true;
            }
            else {
                if(elm.getChildCount() > 0)
                    flag = flag || hasExpression((ParserRuleContext) elm);
            }
        }
        return flag;
    }


    public static ExpressionContext getExpression(ParserRuleContext node){
        ExpressionContext expr = null;
        for (ParseTree elm: node.children ) {
            if ( elm instanceof ExpressionContext ) {
                return (ExpressionContext)elm;
            }
            else {
                if(elm.getChildCount() > 0) {
                    ExpressionContext tmp = getExpression((ParserRuleContext) elm);
                    if(tmp != null){
                        expr = tmp;
                    }
                }
            }
        }
        return expr;
    }

    public static String getTypeStmtExpression(ParserRuleContext node){
        String type = null;
        final Set<String> types = new HashSet<String>(Arrays.asList(
                new String[] {
                        "ReturnStatementContext", "MethodInvocationContext", "AssignmentContext", "UnaryExpressionContext",
                        "LocalVariableDeclarationContext", "PostIncrementExpressionContext"
                }
        ));
        for (ParseTree stmt: node.children) {
            if(stmt instanceof TerminalNode){
                continue;
            }
            else if( types.contains(stmt.getClass().getSimpleName())){
                type = stmt.getClass().getSimpleName();
                type = type.replace("Context", "");
            }
            if(type == null){
                String tmp = getTypeStmtExpression((ParserRuleContext) stmt);
                if(tmp != null)
                    type = tmp;
            }
        }
        return type;
    }

    public static String getExprType(ParserRuleContext node){
        boolean finish = false;
        ParserRuleContext current = node;
        while(!finish){
            if(current.getChildCount() > 1){
                return  current.getClass().toString();
            }
            else if(current instanceof ExpressionNameContext){
                return current.getClass().toString();
            }
            else
                current = (ParserRuleContext) current.getChild(0);
        }
        return "";
    }

    public static Pair<String,ParseTree> getExprTypeWithContext(ExpressionContext node){
        boolean finish = false;
        ParserRuleContext current = node;
        while(!finish){
            if(current.getChildCount() > 1){
                return new Pair(current.getClass().toString(), current);
            }
            else if(current instanceof ExpressionNameContext){
                return new Pair(current.getClass().toString(), current);
            }
            else if(current instanceof LiteralContext){
                return new Pair(current.getClass().toString(), current);
            }
            else //here lays an error to check => TerminalNode casted to ParseRule
                try {
                    current = (ParserRuleContext) current.getChild(0);
                } catch(Exception e){
                    System.out.println(current.getText());
                    throw e;
                }
        }
        return new Pair(null,null);
    }

    public static String getStmtType(ParserRuleContext stmts){
        String type = null;
        for (ParseTree stmt: stmts.children) {
            if(stmt instanceof ReturnStatementContext){
                type = ReturnStatementContext.class.toString();
            }
            else if(stmt instanceof MethodInvocationContext){
                type = MethodInvocationContext.class.toString();
            }
            else if(stmt instanceof AssignmentContext){
                type = AssignmentContext.class.toString();
            }
            else if(stmt instanceof LocalVariableDeclarationContext){
                type = LocalVariableDeclarationContext.class.toString();
            }
            else if(stmt instanceof ExpressionStatementContext){
                type = ExpressionStatementContext.class.toString();
            }
            else if(stmt instanceof TerminalNode){
                continue;
            }
            if(type == null){
                String tmp = getStmtType((ParserRuleContext) stmt);
                if(tmp != null)
                    type = tmp;
            }
        }
        return type;
    }

    /**
     * Remove all the <i>stupid</i> decoration of java to keep track of the type and presenting only a good representation to the user.
     * @param str   The type of the object to make prettier
     * @return      An hence visualization of the character that is faboulousssss
     */
    public static String prettyPrintClassName(String str, ParserRuleContext ctx){
        str = str.substring(str.indexOf("$") + 1).replace("Context","");
        String out = str;
        switch(str){
            case "LocalVariableDeclaration":
                out += prettyPrintLocalVariableDeclaration(ctx);
                break;
            case "MethodInvocation":
                out += prettyPrintMethodInvocation(ctx);
                break;
            case "ReturnStatement":
                out += prettyPrintReturnStatement(ctx);
                break;
            case "ExpressionStatement":
                str = getTypeStmtExpression(ctx);
                out = str;
            default: break;

        }
        return out;
    }

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

    public static String prettyPrintReturnStatement(ParserRuleContext ctx){
        return prettyPrintExpression(ctx);
    }

    public static String prettyPrintExpression(ParserRuleContext ctx){
        String type = null;
        if(ctx instanceof ExpressionContext){
            type = prettyPrintExpression_helper((ExpressionContext)ctx);
        } else {
            for (ParseTree elm : ctx.children ) {
                if(elm instanceof TerminalNode)
                    continue;
                String tmp = prettyPrintExpression((ParserRuleContext) elm);
                if(type == null){
                    type = tmp;
                }
            }
        }
        return type;
    }

    public static String prettyPrintExpression_helper(ExpressionContext expr){
        String out = "nd";
        Pair<String,ParseTree> e = getExprTypeWithContext(expr);
        String expType = e.getFirst();
        expType = expType.substring(expType.indexOf("$") + 1).replace("Context","");
        switch (expType){
            case "MethodInvocation_lfno_primary":
                out = e.getSecond().getChild(2).getText();
                break;
            default:
                break;
        }
        return out;
    }

    public static boolean isIf(ParserRuleContext ctx) {
        boolean f = false;
        for(ParseTree c: ctx.children) {
            if (c instanceof IfThenElseStatementContext ||
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
}
