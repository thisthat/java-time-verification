import IntermediateModel.visitors.CreateIntemediateModel;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.Java2AST;

import java.util.Arrays;
import java.util.List;

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
		String s = Arrays.toString( sv.listOfClasses.toArray() );
		System.out.print(s);
	}

	private static void usage(){
		System.out.println("Usage: {NAME} filename");
	}
}
