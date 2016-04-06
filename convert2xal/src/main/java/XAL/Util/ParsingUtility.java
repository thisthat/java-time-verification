package XAL.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.*;

/**
 * The class exports method that help the parsing of a source file in order to create a XAL document
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ParsingUtility {


    /**
     *
     * @param method
     * @return
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
     *
     * @param ctx
     * @return
     */
    public static List<Pair<String,String>> getParameterList(FormalParameterListContext ctx){
        //FormalParameterContext c = (FormalParameterContext) ctx.getChild(0);
        List<Pair<String,String>> ret = new ArrayList<Pair<String,String>>();
        if(ctx.getChild(0) instanceof LastFormalParameterContext){
            FormalParameterContext par = (FormalParameterContext) ctx.getChild(0).getChild(0);
            ret.add(new Pair<String,String>(getParameterName(par), getParameterType(par)));
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
     *
     * @param par
     * @return
     */
    public static String getParameterName(FormalParameterContext par){
        //System.out.println(par.getChild(1).getText());
        return par.getChild(1).getText();
    }

    /**
     *
     * @param par
     * @return
     */
    public static String getParameterType(FormalParameterContext par){
        return par.getChild(0).getText();
    }
}
