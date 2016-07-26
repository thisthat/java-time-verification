import IntermediateModelHelper.envirorment.Env;
import intermediateModel.structure.*;
import intermediateModel.visitors.ApplyHeuristics;
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
    String filename = "env/AttributeTimeRelated.java";
    List<ASTClass> intemediateModel;

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getClassLoader().getResource(filename).getFile() );
        a.convertToAST(Java2AST.VERSION.JDT);
        CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		intemediateModel = v.listOfClasses;
    }
	public class ApplyHeuristicsTest extends ApplyHeuristics {
		@Override
		public Env indexing(ASTClass c) {
			return super.indexing(c);
		}
	}

    @Test
	public void TestEnvirorment() {
		ApplyHeuristicsTest ah = new ApplyHeuristicsTest();
		//we know there is only one class
		Env e = ah.indexing(intemediateModel.get(0));
	}


}
