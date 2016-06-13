import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.CreateIntemediateModel;
import heuristic.SocketTimeout;
import heuristic.ThreadTime;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;

import java.util.Arrays;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
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
		CreateIntemediateModel sv = new CreateIntemediateModel();

		walker.walk(sv, ast);
		for(ASTClass c : sv.listOfClasses){
			ApplyHeuristics ah = new ApplyHeuristics();
			ThreadTime tt = new ThreadTime();
			SocketTimeout st = new SocketTimeout();
			ah.subscribe(tt);
			ah.subscribe(st);
			ah.analyze(c);
			String s = Arrays.toString( tt.getTimeConstraint().toArray() );
			s += Arrays.toString( st.getTimeConstraint().toArray() );
			System.err.print(s);
			System.err.println("__________");
		}


	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
