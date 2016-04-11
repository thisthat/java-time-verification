package XAL.visitors;


import XAL.exception.XALMalformedException;
import XAL.items.*;
import XAL.util.Pair;
import XAL.util.ParsingUtility;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import parser.grammar.Java8CommentSupportedBaseListener;
import parser.grammar.Java8CommentSupportedParser;
import parser.grammar.Java8CommentSupportedParser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Visitor of the AST that will creates a XAL tree structure.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */
public class CreateXALTree extends Java8CommentSupportedBaseListener {

    boolean __DEBUG__ = false;

    List<XALDocument> documents;
    XALAutomaton current_automata;
    Stack<XALDocument> stackDocument = new Stack<XALDocument>();
    XALDocument current_document;
    static XALState lastState = null;

    public CreateXALTree() {
        documents = new ArrayList<XALDocument>();
    }

    public List<XALDocument>  getOutput(){
        return documents;
    }


    @Override
    public void enterClassDeclaration(@NotNull ClassDeclarationContext ctx) {
        //create document here
        current_document = new XALDocument(ctx.getChild(0).getChild(2).getText());
        documents.add(current_document);
        stackDocument.push(current_document);
        //System.out.println("[ENTER] Now using:" + current_document.getFilename());
    }

    @Override
    public void exitClassDeclaration(@NotNull ClassDeclarationContext ctx) {
        //System.out.println("Leaving.." + ctx.getChild(0).getChild(2).getText());
        if(stackDocument.size() > 0) {
            XALDocument d = stackDocument.pop();
            if (d.equals(current_document) && stackDocument.size() > 0) {
                current_document = stackDocument.peek();
            }
        }
        //System.out.println("[EXIT] Now using:" + current_document.getFilename());
        //System.err.println(d.getFilename());
    }

    public void enterMethodDeclarator(@NotNull MethodDeclaratorContext ctx) {
        //super.enterMethodDeclarator(ctx);
        if(__DEBUG__)
            System.err.println("[" + ctx.getChild(0).getText() + "]");
        XALAutomaton newAutomata = new XALAutomaton(ctx.getChild(0).getText());
        current_document.addAutomaton(newAutomata);
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
    public void enterConstructorDeclarator(@NotNull ConstructorDeclaratorContext ctx) {
        XALAutomaton newAutomata = new XALAutomaton(ctx.getChild(0).getText());
        current_document.addAutomaton(newAutomata);
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
        //System.err.println("LF:" + ctx.getText());
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

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        if(__DEBUG__){
            System.err.println("{I} " + ctx.getClass().getCanonicalName());
        }
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        if(__DEBUG__){
            System.err.println("{O} " + ctx.getClass().getCanonicalName());
        }
    }
}
