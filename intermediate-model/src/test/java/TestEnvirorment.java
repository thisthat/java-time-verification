import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.CreateIntemediateModel;
import envirorment.BuildEnvirormentClass;
import envirorment.Env;
import heuristic.ThreadTime;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestEnvirorment {
    String filename = "Test.java";
    List<ASTClass> intemediateModel;
    List<ASTClass> manuallyCreated = new ArrayList<>();
	BuildEnvirormentClass build_base_env = new BuildEnvirormentClass();

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getClassLoader().getResource(filename).getFile() );
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateIntemediateModel sv = new CreateIntemediateModel();
        walker.walk(sv, ast);
        intemediateModel = sv.listOfClasses;
    }

	/**
	 * TODO: Test that for each instructions the rule of visibility of envirorment is respected.
	 */
    @Test
	public void TestEnvirormentNested() {
		ThreadTime tt = new ThreadTime();
		for(ASTClass c : intemediateModel){
		}
	}


}
