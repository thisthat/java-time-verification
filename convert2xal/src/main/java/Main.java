package XALConversion;

import XALConversion.visitors.CreateXALTree;
import XAL.XALStructure.items.XALDocument;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;

import java.util.List;

/**
 * This is the main class of the program. It accepts as input a filepath and it produces a XAL file from it.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class Main {

    public static int i = 0;
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            usage();
            return;
        }
        Java2AST a = new Java2AST(args[0]);
        a.convertToAST(Java2AST.VERSION.Java_8);
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();

        walker.walk(sv, ast);
        List<XALDocument> out = sv.getOutput();
        for(XALDocument d : out){
            d.toFile();
        }
    }

    private static void usage(){
        System.out.println("Usage: {NAME} filename");
    }
}