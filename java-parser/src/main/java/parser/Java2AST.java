package parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.apache.commons.io.FileUtils.readFileToString;


/**
 * This class handle the opening of a file and its parsing.
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */

public class Java2AST {

    private String filename;
    private boolean isParsed = false;

    //lexer&parser
	private ASTParser parserJDT;
	private char[] source;

	/**
	 * Getter of the AST
	 * @return the AST of the source file
	 */
	public CompilationUnit getContextJDT() {
		return contextJDT;
	}

    private CompilationUnit contextJDT;

    /**
     * Constructor that accept only the file to parse. It will initialize the Lexer and Parser.
     * It does <b>not</b> handle any IO Error
     *
     * @param filename      Path of the file to parse
     * @throws IOException  Exception in the case some filesystem problems will arise
     */
    public Java2AST(String filename) throws IOException {
        this.filename = filename;
        initParser();
    }

    /**
     * Constructor that accept the file to parse and a flag.
     * It will initialize the Lexer and Parser.
     * If the parse flag is true, it will parse the file as well.
     * It does <b>not</b> handle any IO Error
     *
     * @param filename      Path of the file to parse
     * @param parse         Boolean value to decide if the file has to be parsed directly
     * @throws IOException  Exception in the case some filesystem problems will arise
     */
    public Java2AST(String filename, boolean parse) throws IOException {
        this.filename = filename;
        initParser();
        if(parse){
            convertToAST();
        }
    }


    /**
     * Initialization of Lexer & Parser
     * If something goes wrong, it will throw an IO Exception
     *
     * @throws IOException
     */
    private void initParser() throws IOException {

		File file1 = new File(this.filename);
		String source = readFileToString(file1, "utf-8");
		parserJDT = ASTParser.newParser(AST.JLS8);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8
		parserJDT.setKind(ASTParser.K_COMPILATION_UNIT);
		parserJDT.setSource(source.toCharArray());
		parserJDT.setResolveBindings(true);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parserJDT.setCompilerOptions(options);

		this.source = source.toCharArray();
		ASTSrc.getInstance().setSource(this.source);
    }

    /**
     * It converts the java source file into the AST representation.
     */
    public void convertToAST() {
        contextJDT = (CompilationUnit) parserJDT.createAST(null);
        isParsed = true;
		ASTSrc.getInstance().setJDT(contextJDT);
    }

	public char[] getSource() {
		return source;
	}

}
