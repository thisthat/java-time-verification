package testing;


import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Main {

	public static void main(String[] args) throws IOException {
		File file1 = new File(args[0]);
		String source = readFileToString(file1, "utf-8");
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new Visitor());
	}


}
