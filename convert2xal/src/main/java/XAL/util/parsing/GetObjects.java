package XAL.util.parsing;

import XAL.util.Pair;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList;
import com.sun.xml.internal.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.internal.xsom.impl.Ref;
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
        //if we pass at first call an expression
        ExpressionContext expr = (node instanceof ExpressionContext) ? (ExpressionContext) node : null;
        if(expr != null){
            return expr;
        }
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
     * Return the type of a Expression Statement. It assumes that the expression exists since the type checking.
     * @param node  The node from where start the search of the type.
     * @return      The type.
     */
    public static Pair<String,ParseTree> getExprTypeWithContext(ExpressionContext node){
        return getExprTypeWithContext_helper(node);
    }

    private static Pair<String,ParseTree> getExprTypeWithContext_helper(ParserRuleContext ctx){
        Pair<String,ParseTree> result = null;
        for(ParseTree c: ctx.children) {
            if(c.getChildCount() > 1){
                result = new Pair(c.getClass().getSimpleName(), c);
            }
            else if(c instanceof ExpressionNameContext){
                result = new Pair(c.getClass().getSimpleName(), c);
            }
            else if(c instanceof LiteralContext){
                result = new Pair(c.getClass().getSimpleName(), c);
            }
            else if(c instanceof TerminalNode){
                continue;
            }
            else { //here lays an error to check => TerminalNode casted to ParseRule
                ParserRuleContext node = (ParserRuleContext) c;
                Pair<String,ParseTree> r = getExprTypeWithContext_helper(node);
                if(result == null){
                    result = r;
                }
            }
        }
        return result;
    }

    /**
     * Return the type of the statement
     *
     * @param stmts The node to parse to get the type of the Statement.
     * @return      The class name simplified of the statement.
     */
    public static String getStmtType(ParserRuleContext stmts){
        String type = null;
        for (ParseTree stmt: stmts.children) {
            if(stmt instanceof ReturnStatementContext){
                type = ReturnStatementContext.class.getSimpleName();
            }
            else if(stmt instanceof MethodInvocationContext){
                type = MethodInvocationContext.class.getSimpleName();
            }
            else if(stmt instanceof AssignmentContext){
                type = AssignmentContext.class.getSimpleName();
            }
            else if(stmt instanceof LocalVariableDeclarationContext){
                type = LocalVariableDeclarationContext.class.getSimpleName();
            }
            else if(stmt instanceof ExpressionStatementContext){
                type = ExpressionStatementContext.class.getSimpleName();
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
     * Return the name of the last method called in an expression.
     *
     * @param ctx   The node from where start the scan.
     * @return      The name of the last method called in an expression.
     */
    public static String getLastMethodCall(ParserRuleContext ctx) {
        String name = "nd_getLastMethodCall";
        for(ParseTree c: ctx.children) {
            if (c instanceof MethodInvocationContext ||
                    c instanceof MethodInvocation_lfno_primaryContext ||
                    c instanceof MethodInvocation_lf_primaryContext
                )
            {
                //search in the terminal node
                String[] skips = { ".", ",", "(", ")"};
                for(ParseTree t : ((ParserRuleContext) c).children){
                    if( t instanceof TerminalNode){
                        if(!Stream.of(skips).anyMatch(x -> x.equals(t.getText()))){
                            name = t.getText();
                        }
                    }
                    else if(t instanceof MethodNameContext){
                        name = t.getText();
                    }
                }
                return name;
            }
            else if(c instanceof TerminalNodeImpl){
                continue;
            }
            else {
                String tmp = getLastMethodCall((ParserRuleContext) c);
                if(!tmp.equals("nd_getLastMethodCall")){
                    name = tmp;
                }
            }
        }
        return name;
    }


    public static String getNewType(ParserRuleContext ctx){
        String name = "nd_getNewType";
        for(ParseTree c: ctx.children) {
            if(c instanceof ClassInstanceCreationExpression_lfno_primaryContext){
                name = ((ParserRuleContext) c).getChild(1).getText();
            }
            else if(c instanceof TerminalNode){
                continue;
            }
            else {
                String tmp = getNewType((ParserRuleContext) c);
                if(name.equals("nd_getNewType")){
                    name = tmp;
                }
            }
        }
        return name;
    }
}
