package parser;

import org.antlr.v4.runtime.*;
import parser.grammar.Java8CommentSupportedLexer;
import parser.grammar.Java8CommentSupportedParser;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by giovanni (@thisthatDC) on 22/03/16.
 */
public class SingleGrammar {
    static int line_number = 0;

    public static void main(String[] args) throws Exception {
        InputStream in = null;
        String name = "/Users/giovanni/repository/java-parser/testProject/hadoop-2.6.4-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/org/apache/hadoop/yarn/webapp/hamlet/HamletSpec.java";
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/HelloWorld.java";
        Path file = Paths.get(base_path);
        try {
            in = Files.newInputStream(file);
        }
        catch(Exception e){
            System.out.println(name);
        }
        Java8CommentSupportedLexer l = new Java8CommentSupportedLexer(new ANTLRInputStream(in));
        Java8CommentSupportedParser p = new Java8CommentSupportedParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                line_number = line;
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        try {
            ParserRuleContext t = p.compilationUnit();
            t.inspect(p);
        }
        catch(Exception e){
            String err = "Error parsing: " + name + " -- " + line_number;
            //errors.add(err);
            System.err.println(err);
            throw e;//new Exception("Test failed for bhu reason");
        }
        finally {
            l = null;
            p = null;
            System.gc();
        }
    }
}
