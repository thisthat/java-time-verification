package XAL.visitors;


import XAL.exception.XALMalformedException;
import XAL.items.*;
import XAL.util.Pair;
import XAL.util.parsing.*;
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
        List<Pair<String,String>> vars = GetObjects.getParameterList(ctx);
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
    public void enterBasicForStatement(@NotNull BasicForStatementContext ctx) {
        System.err.println("ENTER FOR");

        int indexInit = 2, indexCheck = 4, indexBody = 8, indexUpdate = 6;

        //Init Expression
        if(ctx.getChild(indexInit) instanceof ForInitContext){
            generateState((ParserRuleContext) ctx.getChild(indexInit));
        }
        else {
            indexInit = -1;
            indexCheck--;
            indexBody--;
            indexUpdate--;
        }

        XALState initFor = new XALState("for");
        XALState endFor = new XALState("endfor");
        current_automata.addState(initFor);
        current_automata.addState(endFor);
        XALTransition t = new XALTransition(lastState,initFor);
        current_automata.addTransition(t);
        lastState = initFor;

        //Checks
        if(ctx.getChild(indexCheck) instanceof ExpressionContext) {
            generateStateExpression((ParserRuleContext) ctx.getChild(indexCheck));
            XALTransition toEnd = new XALTransition(lastState, endFor);
            current_automata.addTransition(toEnd);
        }
        else {
            indexCheck = -1;
            indexBody--;
            indexUpdate--;
        }

        if(!(ctx.getChild(indexUpdate) instanceof ForUpdateContext)){
            indexBody--;
            indexUpdate = -1;
        }

        //Body For
        walk(this, ctx.getChild(indexBody));

        //Update
        if(indexUpdate > 0) {
            generateState((ParserRuleContext) ctx.getChild(indexUpdate));
        }
        XALTransition toInit = new XALTransition(lastState, initFor);
        current_automata.addTransition(toInit);
        lastState = endFor;

        if(indexBody > 0)ctx.children.remove(indexBody);
        if(indexUpdate > 0) ctx.children.remove(indexUpdate);
        if(indexCheck > 0)ctx.children.remove(indexCheck);
        if(indexInit > 0)ctx.children.remove(indexInit);

    }

    @Override
    public void enterIfThenStatement(@NotNull IfThenStatementContext ctx) {
        //Expression before
        generateStateExpression((ParserRuleContext) ctx.getChild(2));

        //Create dummy nodes and connect the output of expr to the if dummy node
        XALState s = new XALState("if");
        XALState e = new XALState("endif");
        current_automata.addState(s);
        current_automata.addState(e);
        XALTransition t = new XALTransition(lastState,s);
        current_automata.addTransition(t);
        lastState = s;

        //IF BRANCH
        produceIfElseCode(ctx.getChild(4));


        XALState lastIfBranch = lastState;
        lastState = s;

        //since no else, linked if to endif
        current_automata.addTransition(new XALTransition(s,e));

        //Link the output of each branch to a dummy node
        XALTransition tIf = new XALTransition(lastIfBranch,e);
        current_automata.addTransition(tIf);
        lastState = e;

        //don't visit them anymore
        ctx.children.remove(4);
        ctx.children.remove(2);

        metricValue = null;
    }

    @Override
    public void enterIfThenElseStatement(@NotNull IfThenElseStatementContext ctx) {
        //Expression before
        generateStateExpression((ParserRuleContext) ctx.getChild(2));

        //Create dummy nodes and connect the output of expr to the if dummy node
        XALState s = new XALState("if");
        XALState e = new XALState("endif");
        current_automata.addState(s);
        current_automata.addState(e);
        XALTransition t = new XALTransition(lastState,s);
        current_automata.addTransition(t);
        lastState = s;

        //IF BRANCH
        metricValue = "true";
        produceIfElseCode(ctx.getChild(4));

        XALState lastIfBranch = lastState;
        lastState = s;

        //ELSE BRANCH
        metricValue = "false";
        produceIfElseCode(ctx.getChild(6));
        XALState lastElseBranch = lastState;

        //Link the output of each branch to a dummy node
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

    }

    private void produceIfElseCode(ParseTree ctx){
        if(ctx instanceof StatementContext && !ctx.getText().startsWith("{")){
            //problem if we have inside another construct, like if/while/for/..
            //we have to check if there are inside such construct
            if(Exists.Has2Walk((ParserRuleContext) ctx)){
                walk(this, ctx);
            }
            else {
                generateState((ParserRuleContext) ctx);
            }
        }
        else if(ctx.getText().startsWith("{") &&
                (
                    ctx instanceof StatementContext ||
                    ctx instanceof StatementNoShortIfContext
                )
                ){
            walk(this, ctx.getChild(0));
        }
        else {
            walk(this,ctx);
        }
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        if(__DEBUG__){
            //System.err.println("{I} " + ctx.getClass().getCanonicalName());
        }
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        if(__DEBUG__){
            //System.err.println("{O} " + ctx.getClass().getCanonicalName());
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
        //we have special rules for the if
        if(Exists.Has2Walk(ctx)){
            return;
        }
        String type = GetObjects.getStmtType(ctx);
        XALState state = new XALState( PrettyPrint.prettyPrintClassName(type, ctx) );
        current_automata.addState(state);
        XALTransition transition = new XALTransition(lastState, state);
        current_automata.addTransition(transition);

        if(__DEBUG__){
            System.err.println("[DEBUG} Creating state: " + state.getId() + " :: " + ctx.getText());
            System.err.println("[DEBUG} Creating transition: (" + lastState.getId() + "," + state.getId() + ")");
        }

        lastState = state;
    }

    protected void generateStateExpression(ParserRuleContext ctx){
        XALState state = new XALState( PrettyPrint.prettyPrintExpression(ctx) );
        current_automata.addState(state);
        XALTransition transition = new XALTransition(lastState, state);
        current_automata.addTransition(transition);

        if(__DEBUG__){
            System.err.println("[DEBUG} Creating state: " + state.getId() + " :: " + ctx.getText());
            System.err.println("[DEBUG} Creating transition: (" + lastState.getId() + "," + state.getId() + ")");
        }

        lastState = state;
    }


}
