import IntermediateModel.interfaces.ASTSrc;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.CreateIntemediateModel;
import IntermediateModel.visitors.JDTVisitor;
import heuristic.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.util.Arrays;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void java8_main(String[] args) throws Exception {

		if(args.length < 1){
			usage();
			return;
		}
		Java2AST a = new Java2AST(args[0]);
		a.convertToAST(Java2AST.VERSION.Java_8);
		ParserRuleContext ast = a.getContext();
		ParseTreeWalker walker = new ParseTreeWalker();
		CreateIntemediateModel sv = new CreateIntemediateModel();
		System.out.println("Create IM");
		walker.walk(sv, ast);


		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);

		for(ASTClass c : sv.listOfClasses){

			ah.analyze(c);
			String s = Arrays.toString( ah.getTimeConstraint().toArray() );
			System.err.println(s);
			System.err.println("__________");
		}


	}

	public static void main(String[] args) throws Exception {

		if(args.length < 1){
			usage();
			return;
		}
		Java2AST a = new Java2AST(args[0], Java2AST.VERSION.JDT, true);
		CompilationUnit ast = a.getContextJDT();
		ASTSrc.getInstance().setSource(a.getSource());
		JDTVisitor v = new JDTVisitor(ast);
		ast.accept(v);


		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);

		/*
		for(ASTClass c : sv.listOfClasses){

			ah.analyze(c);
			String s = Arrays.toString( ah.getTimeConstraint().toArray() );
			System.err.println(s);
			System.err.println("__________");
		} */


	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
