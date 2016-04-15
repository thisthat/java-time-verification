package XAL.util.parsing;

import XAL.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GetObjects {

    /**
     * Collect all the variables and their types from a method definition.
     *
     * @param ctx   The list of parameters
     * @return      A list of {@link Pair} contain &lt;Name, Type&gt;
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
     *
     * @param par   The parameter in object
     * @return      Its name
     */
    public static String getParameterName(FormalParameterContext par) {
        //System.out.println(par.getChild(1).getText());
        return par.getChild(1).getText();
    }

    /**
     * Extract the type of a parameter
     *
     * @param par   The parameter in object
     * @return      Its type
     */
    public static String getParameterType(FormalParameterContext par){
        return par.getChild(0).getText();
    }


    /**
     * Return the first expression in the tree passed as parameter.
     *
     * @param node  The node of the AST from where start to search for the expression.
     * @return      The Expression || null.
     */
    public static ExpressionContext getExpression(ParserRuleContext node){
        ExpressionContext expr = null;
        for (ParseTree elm: node.children ) {
            if ( elm instanceof ExpressionContext) {
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

    /**
     * Return the type of a Expression Statement. It assumes that the expression exists.
     *
     * @param node  The node from where start the search of the type.
     * @return      The type.
     */
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

    /**
     *
     * @param node
     * @return
     */
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

    /**
     * Return the type of a Expression Statement. It assumes that the expression exists.
     * @TODO REWRITE
     * @param node  The node from where start the search of the type.
     * @return      The type.
     */
    public static Pair<String,ParseTree> getExprTypeWithContext(ExpressionContext node){
        boolean finish = false;
        ParserRuleContext current = node;
        while(!finish){
            if(current.getChildCount() > 1){
                return new Pair(current.getClass().getSimpleName(), current);
            }
            else if(current instanceof ExpressionNameContext){
                return new Pair(current.getClass().getSimpleName(), current);
            }
            else if(current instanceof LiteralContext){
                return new Pair(current.getClass().getSimpleName(), current);
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

    /**
     *
     * @param stmts
     * @return
     */
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
     *
     * @param ctx
     * @return
     */
    public static String getLastMethodCall(ParserRuleContext ctx) {
        String name = "ndm";
        for(ParseTree c: ctx.children) {
            if (c instanceof MethodInvocationContext ||
                    c instanceof MethodInvocation_lfno_primaryContext ||
                    c instanceof MethodInvocation_lf_primaryContext)
            {
                //search in the terminal node
                String[] skips = { ".", ",", "(", ")"};
                for(ParseTree t : ((ParserRuleContext) c).children){
                    if( t instanceof TerminalNode){
                        if(!Stream.of(skips).anyMatch(x -> x.equals(t.getText()))){
                            name = t.getText();
                        }
                    }
                }
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                name = getLastMethodCall((ParserRuleContext) c);
            }
        }
        return name;
    }
}
