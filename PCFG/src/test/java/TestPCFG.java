import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import parser.Java2AST;
import structure.Node;
import structure.PCFG;
import visitors.IM2PCFG;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestPCFG {

	@Test
	public void TestRE() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("Re.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestDoWhile() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("DoWhile.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s1;\n" +
				"s5 -> s6;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestFor() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("For.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s6;\n" +
				"s6 -> s7;\n" +
				"s7 -> s3;\n" +
				"s3 -> s8;\n" +
				"s8 -> s9;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestWhile() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("While.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"s4;\n" +
				"s5;\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s4;\n" +
				"s4 -> s1;\n" +
				"s1 -> s5;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestSwitch() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("Switch.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s2;\n" +
				"s1;\n" +
				"s3;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s10;\n" +
				"s11;\n" +
				"s12;\n" +
				"s0 -> s1;\n" +
				"s1 -> s3;\n" +
				"s3 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s6;\n" +
				"s6 -> s2;\n" +
				"s3 -> s7;\n" +
				"s7 -> s8;\n" +
				"s8 -> s2;\n" +
				"s3 -> s9;\n" +
				"s9 -> s10;\n" +
				"s10 -> s11;\n" +
				"s11 -> s2;\n" +
				"s2 -> s12;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestForEach() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("ForEach.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"s4;\n" +
				"s5;\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s1;\n" +
				"s1 -> s4;\n" +
				"s4 -> s5;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestSync() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("Sync.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"s4;\n" +
				"subgraph cluster_1 {\n" +
				"node [style=filled];\n" +
				"s1;\n" +
				"s2;\n" +
				"s3;\n" +
				"label = \"sync block 1\";\n" +
				"color=blue\n" +
				"}\n" +
				"s0 -> s1;\n" +
				"s1 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s4;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestIf() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("If.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s3;\n" +
				"s2;\n" +
				"s4;\n" +
				"s0 -> s1;\n" +
				"s1 -> s3;\n" +
				"s3 -> s2;\n" +
				"s1 -> s2;\n" +
				"s2 -> s4;\n" +
				"}", graph.trim());
		Node._ID = 0;

		// check the second method
		m = c.getMethods().get(1);
		// Build the PCFG
		p = new IM2PCFG();
		p.addClass(c, m.getName());
		g = p.buildPCFG();
		graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s3;\n" +
				"s2;\n" +
				"s4;\n" +
				"s0 -> s1;\n" +
				"s1 -> s3;\n" +
				"s3 -> s2;\n" +
				"s1 -> s2;\n" +
				"s2 -> s4;\n" +
				"}", graph.trim());
		Node._ID = 0;

		// check the second method
		m = c.getMethods().get(2);
		// Build the PCFG
		p = new IM2PCFG();
		p.addClass(c, m.getName());
		g = p.buildPCFG();
		graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s1;\n" +
				"s3;\n" +
				"s4;\n" +
				"s2;\n" +
				"s5;\n" +
				"s0 -> s1;\n" +
				"s1 -> s3;\n" +
				"s3 -> s2;\n" +
				"s1 -> s4;\n" +
				"s4 -> s2;\n" +
				"s2 -> s5;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestTry() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("Try.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s2;\n" +
				"s3;\n" +
				"s1;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s10;\n" +
				"s11;\n" +
				"s0 -> s1;\n" +
				"s1 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s2;\n" +
				"s1 -> s6;\n" +
				"s6 -> s7;\n" +
				"s7 -> s8;\n" +
				"s8 -> s2;\n" +
				"s7 -> s2;\n" +
				"s6 -> s9;\n" +
				"s9 -> s10;\n" +
				"s10 -> s2;\n" +
				"s9 -> s2;\n" +
				"s2 -> s3;\n" +
				"s3 -> s11;\n" +
				"}", graph.trim());
		Node._ID = 0;

		// check the second method
		m = c.getMethods().get(1);
		// Build the PCFG
		p = new IM2PCFG();
		p.addClass(c, m.getName());
		g = p.buildPCFG();
		graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s2;\n" +
				"s3;\n" +
				"s1;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s10;\n" +
				"s0 -> s1;\n" +
				"s1 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s2;\n" +
				"s1 -> s6;\n" +
				"s6 -> s7;\n" +
				"s7 -> s8;\n" +
				"s8 -> s2;\n" +
				"s7 -> s2;\n" +
				"s2 -> s9;\n" +
				"s9 -> s3;\n" +
				"s3 -> s10;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test
	public void TestTryResource() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("TryResource.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod m = c.getMethods().get(0);

		// Build the PCFG
		IM2PCFG p = new IM2PCFG();
		p.addClass(c, m.getName());
		PCFG g = p.buildPCFG();
		String graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s2;\n" +
				"s3;\n" +
				"s1;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s10;\n" +
				"s11;\n" +
				"s12;\n" +
				"s0 -> s1;\n" +
				"s1 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s2;\n" +
				"s1 -> s6;\n" +
				"s6 -> s7;\n" +
				"s7 -> s8;\n" +
				"s8 -> s2;\n" +
				"s7 -> s2;\n" +
				"s6 -> s9;\n" +
				"s9 -> s10;\n" +
				"s10 -> s2;\n" +
				"s9 -> s2;\n" +
				"s2 -> s11;\n" +
				"s11 -> s3;\n" +
				"s3 -> s12;\n" +
				"}", graph.trim());
		Node._ID = 0;

		// check the second method
		m = c.getMethods().get(1);
		// Build the PCFG
		p = new IM2PCFG();
		p.addClass(c, m.getName());
		g = p.buildPCFG();
		graph = g.toGraphViz(true);
		assertEquals("digraph {\n" +
				"rankdir=LR;\n" +
				"s0;\n" +
				"s2;\n" +
				"s3;\n" +
				"s1;\n" +
				"s4;\n" +
				"s5;\n" +
				"s6;\n" +
				"s7;\n" +
				"s8;\n" +
				"s9;\n" +
				"s10;\n" +
				"s0 -> s1;\n" +
				"s1 -> s4;\n" +
				"s4 -> s5;\n" +
				"s5 -> s2;\n" +
				"s1 -> s6;\n" +
				"s6 -> s7;\n" +
				"s7 -> s2;\n" +
				"s7 -> s2;\n" +
				"s2 -> s8;\n" +
				"s8 -> s9;\n" +
				"s9 -> s3;\n" +
				"s3 -> s10;\n" +
				"}", graph.trim());
		Node._ID = 0;
	}

	@Test(expected = AssertionError.class)
	public void TestGoTo() throws Exception {
		String f =  TestPCFG.class.getClassLoader().getResource("GoTo.java").getFile();
		Java2AST a = new Java2AST(f, Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);
		// we have only one class
		ASTClass c = v.listOfClasses.get(0);
		// and we have only one method
		IASTMethod mContinue = c.getMethods().get(0);
		IASTMethod mBreak 	 = c.getMethods().get(1);
		IASTMethod mReturn 	 = c.getMethods().get(2);
		IASTMethod mThrow 	 = c.getMethods().get(3);

		IM2PCFG pContinue 	= new IM2PCFG();
		IM2PCFG pBreak 	  	= new IM2PCFG();
		IM2PCFG pReturn 	= new IM2PCFG();
		IM2PCFG pThrow 		= new IM2PCFG();
		pContinue.addClass(	c, mContinue.getName());
		pBreak.addClass(	c, mBreak.getName());
		pReturn.addClass(	c, mReturn.getName());
		pThrow.addClass(	c, mThrow.getName());
		Node._ID = 0;
		PCFG gContinue 	= pContinue.buildPCFG();
		Node._ID = 0;
		PCFG gBreak 	= pBreak.buildPCFG();
		Node._ID = 0;
		PCFG gReturn 	= pReturn.buildPCFG();
		Node._ID = 0;
		PCFG gThrow 	= pThrow.buildPCFG();
		Node._ID = 0;
		//optimize to make the goto expression go where they should go
		gContinue.optimize();
		gBreak.optimize();
		gReturn.optimize();
		gThrow.optimize();
		String graphContinue 	= gContinue.toGraphViz(true);
		String graphBreak 		= gBreak.toGraphViz(true);
		String graphReturn 		= gReturn.toGraphViz(true);
		String graphThrow 		= gThrow.toGraphViz(true);

		//TODO implement these checks when optimize is done
		assertEquals(true, false);
		Node._ID = 0;
	}
}
