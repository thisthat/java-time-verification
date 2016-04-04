package parser;

import org.antlr.v4.runtime.*;
import parser.grammar.Java8CommentSupportedLexer;
import parser.grammar.Java8CommentSupportedParser;


/**
 * This class calculate the ms used to parse a Java file.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class TimeParsing {
    public static void main(String[] args){
        CharStream in = null;
        try {
            in =  new ANTLRFileStream(args[0]);
        }
        catch(Exception e){
            System.out.println(args[0]);
        }

        Java8CommentSupportedLexer l = null;
        try {
            l = new Java8CommentSupportedLexer(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Java8CommentSupportedParser p = new Java8CommentSupportedParser(new CommonTokenStream(l));
        long start = System.currentTimeMillis();
        p.setErrorHandler(new DefaultErrorStrategy());
        //p.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        ParserRuleContext t = p.compilationUnit();
        //t.inspect(p);
        long end = System.currentTimeMillis();
        System.out.println("Time Required: " + (end - start));
    }
}
