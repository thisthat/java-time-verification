import IntermediateModel.structure.ASTClass;
import IntermediateModel.visitors.ApplyHeuristics;
import IntermediateModel.visitors.CreateIntemediateModel;
import heuristic.ThreadTime;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;


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
		ThreadTime tt = new ThreadTime();
		for(ASTClass c : sv.listOfClasses){
			ApplyHeuristics ah = new ApplyHeuristics();
			ah.subscribe(tt);
			ah.analyze(c);
			System.err.println("__________");
		}
		//String s = Arrays.toString( sv.listOfClasses.toArray() );
		//System.out.print(s);
	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
