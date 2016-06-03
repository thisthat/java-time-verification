import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.*;
import IntermediateModel.visitors.CreateIntemediateModel;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestClass {
    String filename = "ExportChangesJob.java";
    List<ASTClass> intemediateModel;
    List<ASTClass> manuallyCreated = new ArrayList<>();

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getResource(filename).getFile() );
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateIntemediateModel sv = new CreateIntemediateModel();
        walker.walk(sv, ast);
        intemediateModel = sv.listOfClasses;
    }


    @Test
    public void name() throws Exception {
        assertEquals(true, false);
    }

    @Test
    public void a2() throws Exception {
        assertEquals(true, !false);
    }
}
