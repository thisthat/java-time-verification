import XAL.XALStructure.exception.XALMalformedException;
import XAL.XALStructure.items.XALAutomaton;
import XAL.XALStructure.items.XALDocument;
import XAL.XALStructure.items.XALState;
import XAL.XALStructure.items.XALTransition;
import XALConversion.visitors.CreateXALTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestFor {

    XALDocument xal;

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getResource("For.java").getFile() );
        a.convertToAST(Java2AST.VERSION.Java_8);
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();
        walker.walk(sv, ast);
        xal = sv.getOutput().get(0);
        removeNamingIssue(xal);
    }

    /**
     * This test expected that all the automata have the same content, expect the automata id
     * @throws Exception
     */
    @Test
    public void TestSimpleEqualComplex() throws Exception {
        List<XALAutomaton> simple = new ArrayList<>();
        List<XALAutomaton> complex = new ArrayList<>();
        for(XALAutomaton a: xal.getAutomatons()){
            String id = a.getId();
            if(id.startsWith("simple")){
                simple.add(a);
            } else {
                complex.add(a);
            }
        }
        assertEquals(simple.size(), complex.size());
        int n = simple.size();
        for(int i = 0; i < n; i++){
            String s = simple.get(i).toString();
            s = s.substring( s.indexOf("\n") );
            String c = simple.get(i).toString();
            c = c.substring( c.indexOf("\n") );
            assertEquals( s , c  );
        }
    }

    private void removeNamingIssue(XALDocument d){
        for(XALAutomaton a: d.getAutomatons()){
            removeNamingIssue(a);
        }
    }

    private void removeNamingIssue(XALAutomaton a) {
        Map<String,String> renaming = new HashMap<String,String>();
        int i = 0;
        //collect the renaming
        for(XALState s : a.getStates()){
            renaming.put(s.getId(), "state_" + i++);
        }
        //apply the renaming
        for(XALState s : a.getStates()){
            s.setId( renaming.get( s.getId() ) );
        }
        for(XALTransition t : a.getTransitions()){
            t.setFrom( renaming.get( t.getFrom() ) );
            t.setTo( renaming.get( t.getTo() ) );
        }
        try {
            a.setInitialState( renaming.get( a.getInitialState()) );
        } catch (XALMalformedException e) {
            //it could never be here the control flow
        }
        List<String> end = new ArrayList<String>();
        for(String f : a.getFinalStates()){
            end.add( renaming.get(f) );
        }
        a.setFinalStates(end);
    }


}
