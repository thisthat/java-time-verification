import intermediateModel.structure.*;
import intermediateModel.visitors.JDTVisitor;
import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestEnvirorment {
    String filename = "Test.java";
    List<ASTClass> intemediateModel;
    List<ASTClass> manuallyCreated = new ArrayList<ASTClass>();
	BuildEnvirormentClass build_base_env = new BuildEnvirormentClass();

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getClassLoader().getResource(filename).getFile() );
        a.convertToAST(Java2AST.VERSION.JDT);
        CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		intemediateModel = v.listOfClasses;
    }

	/**
	 * TODO: Test that for each instructions the rule of visibility of envirorment is respected.
	 */
    @Test
	public void TestEnvirormentNested() {

	}


}
