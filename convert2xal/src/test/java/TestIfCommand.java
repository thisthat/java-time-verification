import XAL.items.XALDocument;
import XAL.visitors.CreateXALTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestIfCommand {

    XALDocument xal;

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getResource("If.java").getFile() );
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();
        walker.walk(sv, ast);
        xal = sv.getOutput().get(0);
    }

    @Test
    public void TestIf() throws Exception {
        String out = xal.getAutomatons().get(0).toString();
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/If/TestIf.xal"),
                "UTF-8"
        );
        assertEquals(out,expect);
    }

    @Test
    public void TestIfElse() throws Exception {
        String out = xal.getAutomatons().get(1).toString();
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/If/TestIfElse.xal"),
                "UTF-8"
        );
        assertEquals(out,expect);
    }

    @Test
    public void TestIfBracket() throws Exception {
        String out = xal.getAutomatons().get(2).toString();
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/If/TestIfBracket.xal"),
                "UTF-8"
        );
        assertEquals(out,expect);
    }

    @Test
    public void TestIfElseBracket() throws Exception {
        String out = xal.getAutomatons().get(3).toString();
        out = out.replaceAll("style=\"(.*)\"", "");
        String expect = IOUtils.toString(
                this.getClass().getResourceAsStream("expected/If/TestIfElseBracket.xal"),
                "UTF-8"
        );
        assertEquals(out,expect);
    }


}
