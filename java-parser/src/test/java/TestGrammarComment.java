import org.antlr.v4.runtime.*;
import org.junit.Test;
import parser.grammar.Java8CommentSupportedLexer;
import parser.grammar.Java8CommentSupportedParser;

/**
 * Created by giovanni (@thisthatDC) on 18/03/16.
 */
public class TestGrammarComment {

    public void testGrammarByName(String name) throws Exception {
        Java8CommentSupportedLexer l = new Java8CommentSupportedLexer(new ANTLRInputStream(getClass().getResourceAsStream(name)));
        Java8CommentSupportedParser p = new Java8CommentSupportedParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        try {
            ParserRuleContext t = p.compilationUnit();
            //t.inspect(p);
            //System.out.println(t.toStringTree(p));
        }
        catch(Exception e){
            System.err.println("NO CLASS");
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            System.err.println(stackTraceElements[2]);
            System.err.println("____________");
            throw e;//new Exception("Test failed for bhu reason");
        }
        //System.err.println("____________");
    }

    @Test
    public void testExampleField() throws Exception {
        testGrammarByName("/HelloWorld.java");
    }

    @Test
    public void testAntlr4Mojo() throws Exception {
        testGrammarByName("/Antlr4Mojo.java");
    }

    @Test
    public void testCalculator() throws Exception {
        testGrammarByName("/Calculator.java");
    }

    @Test
    public void testLocalDiscovery() throws Exception {
        testGrammarByName("/LocalDiscovery.java");
    }

    @Test(expected=Exception.class)
    public void testOnlyMethod() throws Exception {
        testGrammarByName("/OnlyMethod.java");
    }

    @Test
    public void testPredictionModule() throws Exception {
        testGrammarByName("/PredictionModule.java");
    }

    @Test
    public void testSchedulerImpl() throws Exception {
        testGrammarByName("/SchedulerImpl.java");
    }

}
