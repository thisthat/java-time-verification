package XAL.visitors;


import XAL.exception.XALMalformedException;
import XAL.items.*;
import XAL.util.Pair;
import XAL.util.ParsingUtility;
import org.antlr.v4.runtime.misc.NotNull;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser.*;

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
    static XALState lastState = null;

    public CreateXALTree() {
        document = new XALDocument();
    }

    public String getOutput(){
        return document.toString();
    }


    public void enterMethodDeclarator(@NotNull MethodDeclaratorContext ctx) {
        //super.enterMethodDeclarator(ctx);
        XALAutomaton newAutomata = new XALAutomaton(ctx.getChild(0).toString());
        document.addAutomaton(newAutomata);
        current_automata = newAutomata;
        XALState init = new XALState("init");
        lastState = init;
        current_automata.addState(init);
        try {
            current_automata.setInitialState(init);
        } catch (XALMalformedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exitMethodDeclaration(@NotNull MethodDeclarationContext ctx) {
        current_automata.addFinalState(lastState);
    }

    @Override
    public void enterFormalParameterList(@NotNull FormalParameterListContext ctx) {
        super.enterFormalParameterList(ctx);
        List<Pair<String,String>> vars = ParsingUtility.getParameterList(ctx);
        for(Pair<String,String> var : vars){
            current_automata.getGlobalState().addVariable( new XALVariable( var.getFirst(), var.getSecond(), "", XALVariable.XALIO.I) );
        }
    }

    @Override
    public void enterMethodInvocation_lf_primary(@NotNull MethodInvocation_lf_primaryContext ctx) {
        System.err.println("LF:" + ctx.getText());
    }

    @Override
    public void enterBlockStatement(@NotNull BlockStatementContext ctx) {
        String type = ParsingUtility.getStmtType(ctx);

        XALState state = new XALState( ParsingUtility.prettyPrintClassName(type, ctx) );
        XALTransition transition = new XALTransition(lastState, state);
        current_automata.addState(state);
        current_automata.addTransition(transition);
        lastState = state;
    }

    @Override
    public void enterMethodInvocation_lfno_primary(@NotNull MethodInvocation_lfno_primaryContext ctx) {
        //System.err.println(ctx.getText());
    }
}
