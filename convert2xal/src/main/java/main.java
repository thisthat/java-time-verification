import XAL.items.XALDocument;
import XAL.visitors.CreateXALTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;
import parser.visitors.SimpleVisitor;

/**
 * This is the main class of the program. It accepts as input a filepath and it produces a XAL file from it.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            usage();
            return;
        }
        Java2AST a = new Java2AST(args[0]);
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();
        walker.walk(sv, ast);
        System.out.println(sv.getOutput());
    }

    private static void usage(){
        System.out.println("Usage: {NAME} filename");
    }
}
