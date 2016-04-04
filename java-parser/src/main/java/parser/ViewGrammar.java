package parser;

import org.antlr.v4.runtime.*;
import parser.grammar.Java8CommentSupportedLexer;
import parser.grammar.Java8CommentSupportedParser;

import java.io.IOException;

/**
 * Class to view in a GUI the parsed tree
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class ViewGrammar {

    public static void main(String[] args){
        CharStream in = null;
        try {
            in = new ANTLRFileStream(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Java8CommentSupportedLexer l = null;
        try {
            l = new Java8CommentSupportedLexer(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Java8CommentSupportedParser p = new Java8CommentSupportedParser(new CommonTokenStream(l));
        try {
            ParserRuleContext t = p.compilationUnit();
            t.inspect(p);
            System.out.println(t.toStringTree(p));
        }
        catch(Exception e){
            System.err.println("NO CLASS");
        }
    }
}
