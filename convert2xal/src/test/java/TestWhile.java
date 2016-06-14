import XALConversion.visitors.CreateXALTree;
import XALStructure.exception.XALMalformedException;
import XALStructure.items.XALAutomaton;
import XALStructure.items.XALDocument;
import XALStructure.items.XALState;
import XALStructure.items.XALTransition;
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
public class TestWhile {

    XALDocument xal;

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getResource("While.java").getFile() );
        a.convertToAST(Java2AST.VERSION.Java_8);
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();
        walker.walk(sv, ast);
        xal = sv.getOutput().get(0);
        removeNamingIssue(xal);
    }


    @Test
    public void TestSimple() throws Exception {
        String out = xal.getAutomatons().get(0).toString().replace("\t","    ");
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/while/simpleWhile.xal"),
                "UTF-8"
        ).replaceAll("style=\"(.*)\"", "");
        assertEquals(out,expect);
    }

    @Test
    public void TestSingle() throws Exception {
        String out = xal.getAutomatons().get(1).toString().replace("\t","    ");
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/while/singleWhile.xal"),
                "UTF-8"
        ).replaceAll("style=\"(.*)\"", "");
        assertEquals(out,expect);
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
