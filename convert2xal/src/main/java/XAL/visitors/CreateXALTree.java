package XAL.visitors;


import XAL.exception.XALMalformedException;
import XAL.items.*;
import XAL.util.Pair;
import XAL.util.ParsingUtility;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;
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

    boolean __DEBUG__ = true;

    List<XALDocument> documents;
    XALAutomaton current_automata;
    Stack<XALDocument> stackDocument = new Stack<XALDocument>();
    XALDocument current_document;
    static XALState lastState = null;
    String metricValue = null;

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
        generateState(ctx);
    }


    @Override
    public void enterStatementNoShortIf(@NotNull StatementNoShortIfContext ctx) {
        generateState(ctx);
    }

    @Override
    public void enterMethodInvocation_lfno_primary(@NotNull MethodInvocation_lfno_primaryContext ctx) {
        //System.err.println(ctx.getText());
    }

    @Override
    public void enterIfThenStatement(@NotNull IfThenStatementContext ctx) {
        //child(2) -> expr
        //child(4) -> if branch
        walk(this,ctx.getChild(2));
        XALState s = new XALState("if");
        current_automata.addState(s);
        XALTransition t = new XALTransition(lastState,s);
        current_automata.addTransition(t);
        lastState = s;
        walk(this,ctx.getChild(4));
        lastState = s;
        //don't visit them anymore
        ctx.children.remove(4);
        ctx.children.remove(2);
    }

    @Override
    public void enterIfThenElseStatement(@NotNull IfThenElseStatementContext ctx) {
        //child(2) - expr
        //child(4) - if branch
        //child(6) - else branch

        System.err.println("Before EXP");
        walk(this,ctx.getChild(2));
        System.err.println("After EXP");


        XALState s = new XALState("if");
        XALState e = new XALState("endif");
        current_automata.addState(s);
        current_automata.addState(e);
        XALTransition t = new XALTransition(lastState,s);
        current_automata.addTransition(t);
        lastState = s;

        System.err.println("Enter IF Branch");
        metricValue = "true";
        walk(this,ctx.getChild(4));
        System.err.println("Exit IF Branch");
        XALState lastIfBranch = lastState;
        lastState = s;

        System.err.println("Enter Else Branch");
        metricValue = "false";
        walk(this,ctx.getChild(6));
        System.err.println("Exit Else Branch");
        XALState lastElseBranch = lastState;

        XALTransition tIf = new XALTransition(lastIfBranch,e);
        current_automata.addTransition(tIf);
        XALTransition tElse = new XALTransition(lastElseBranch,e);
        current_automata.addTransition(tElse);

        lastState = e;

        //don't visit them anymore
        ctx.children.remove(6);
        ctx.children.remove(4);
        ctx.children.remove(2);

        metricValue = null;

        System.err.println("EXIT IF");
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

    public void walk(ParseTreeListener listener, ParseTree t) {
        if(t instanceof ErrorNode) {
            listener.visitErrorNode((ErrorNode)t);
        } else if(t instanceof TerminalNode) {
            listener.visitTerminal((TerminalNode)t);
        } else {
            RuleNode r = (RuleNode)t;
            this.enterRule(listener, r);
            int n = r.getChildCount();
            for(int i = 0; i < n; ++i) {
                this.walk(listener, r.getChild(i));
            }

            this.exitRule(listener, r);
        }
    }

    protected void enterRule(ParseTreeListener listener, RuleNode r) {
        ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
        listener.enterEveryRule(ctx);
        ctx.enterRule(listener);
    }

    protected void exitRule(ParseTreeListener listener, RuleNode r) {
        ParserRuleContext ctx = (ParserRuleContext)r.getRuleContext();
        ctx.exitRule(listener);
        listener.exitEveryRule(ctx);
    }

    protected void generateState(ParserRuleContext ctx){
        //skip if it is an if
        if(ParsingUtility.isIf(ctx)){
            return;
        }
        String type = ParsingUtility.getStmtType(ctx);
        XALState state = new XALState( ParsingUtility.prettyPrintClassName(type, ctx) );
        current_automata.addState(state);
        System.err.println("[DEBUG} Creating state: " + state.getId() + " :: " + ctx.getText());
        XALTransition transition = new XALTransition(lastState, state, metricValue);
        current_automata.addTransition(transition);
        System.err.println("[DEBUG} Creating transition: (" + lastState.getId() + "," + state.getId() + ")");
        lastState = state;
    }
}
