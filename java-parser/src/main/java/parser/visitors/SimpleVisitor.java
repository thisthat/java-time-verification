package parser.visitors;

import org.antlr.v4.runtime.misc.NotNull;
import parser.grammar.*;

/**
 * Simple visitor that accesses only to the root element
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class SimpleVisitor extends Java8CommentSupportedBaseListener {
    String output = "";

    @Override
    public void enterCompilationUnit(@NotNull Java8CommentSupportedParser.CompilationUnitContext ctx) {
        super.enterCompilationUnit(ctx);
        output += ctx.getText().replace("<EOF>","");
        //System.out.println(output);
    }

    public String getOutput(){
        return output;
    }
}
