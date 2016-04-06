package XAL.visitors;


import XAL.items.XALVariable;
import XAL.util.Pair;
import XAL.util.ParsingUtility;
import XAL.items.XALAutomaton;
import XAL.items.XALDocument;
import org.antlr.v4.runtime.misc.NotNull;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.Arrays;
import java.util.List;


/**
 * Visitor of the AST that will creates a XAL tree structure.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */
public class CreateXALTree extends Java8CommentSupportedBaseListener {

    XALDocument document;
    XALAutomaton current_automata;

    public CreateXALTree() {
        document = new XALDocument();
    }

    public String getOutput(){
        return document.toString();
    }


    @Override
    public void enterMethodDeclarator(@NotNull MethodDeclaratorContext ctx) {
        super.enterMethodDeclarator(ctx);
        XALAutomaton newAutomata = new XALAutomaton(ctx.getChild(0).toString());
        document.addAutomaton(newAutomata);
        current_automata = newAutomata;
    }

    @Override
    public void enterFormalParameterList(@NotNull FormalParameterListContext ctx) {
        super.enterFormalParameterList(ctx);
        List<Pair<String,String>> vars = ParsingUtility.getParameterList(ctx);
        System.err.println(Arrays.toString(vars.toArray()));

        for(Pair<String,String> var : vars){
            current_automata.getGlobalState().addVariable( new XALVariable( var.getFirst(), var.getSecond(), "", XALVariable.XALIO.I) );
        }

    }
}
