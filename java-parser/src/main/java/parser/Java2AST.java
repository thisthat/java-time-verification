package parser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.exception.ParseErrorsException;
import org.antlr.v4.runtime.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parser.grammar.*;

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
    private Java8CommentSupportedLexer lexer;
    private Java8CommentSupportedParser parser;
	private Java7Lexer  lexer7;
    private Java7Parser parser7;
	private ASTParser parserJDT;
	private char[] source;

	public enum VERSION {
		Java_7,
		Java_8,
		JDT
	}

    /**
     * Getter of the AST
     * @return the AST of the source file
     */
    public ParserRuleContext getContext() {
        return context;
    }

	/**
	 * Getter of the AST
	 * @return the AST of the source file
	 */
	public CompilationUnit getContextJDT() {
		return contextJDT;
	}

	private ParserRuleContext context;
    private CompilationUnit contextJDT;

    //Error list
    private List<ParseError> errorList = new ArrayList<ParseError>();

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
     * @throws ParseErrorsException Exception that contains all the parsing errors
     */
    public Java2AST(String filename, VERSION v, boolean parse) throws IOException, ParseErrorsException {
        this.filename = filename;
        initParser();
        if(parse){
            convertToAST(v);
        }
    }


    /**
     * Inizialization of ANTLRv4 Lexer & Parser
     * If something goes wrong, it will throw an IO Exception
     * The parser's error strategy is to store all the founded errors in a List
     *
     * @throws IOException
     */
    private void initParser() throws IOException {
        CharStream in = null;
        try {
            in = new ANTLRFileStream(this.filename);
        } catch (IOException e) {
            throw e;
        }

		lexer7 = new Java7Lexer(in);
		parser7 = new Java7Parser(new CommonTokenStream(lexer7));
		parser7.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				ParseError err = new ParseError(line, charPositionInLine, msg);
				errorList.add(err);
				//throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
			}
		});

		lexer = new Java8CommentSupportedLexer(in);
		parser = new Java8CommentSupportedParser(new CommonTokenStream(lexer));
		parser.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				ParseError err = new ParseError(line, charPositionInLine, msg);
				errorList.add(err);
				//throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
			}
		});

		File file1 = new File(this.filename);
		String source = readFileToString(file1, "utf-8");
		parserJDT = ASTParser.newParser(AST.JLS8);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7
		parserJDT.setKind(ASTParser.K_COMPILATION_UNIT);
		parserJDT.setSource(source.toCharArray());
		parserJDT.setResolveBindings(true);

		this.source = source.toCharArray();
		ASTSrc.getInstance().setSource(this.source);
    }


    /**
     * Getter that returns the list of errors found while parsing
     * @return  List of parsing errors
     */
    public List<ParseError> getErorrs(){return errorList;}


    /**
     * It converts the java source file into the AST representation.
     * If there are parsing errors, it will throw an Exception.
     *
     * @throws ParseErrorsException Exception that contains all the parsing errors
     */
    public void convertToAST(VERSION v) throws ParseErrorsException {
        errorList.clear();
		if(v == VERSION.Java_7)
        	context = parser7.compilationUnit();
		else if(v == VERSION.Java_8)
			context = parser.compilationUnit();
		else
			contextJDT = (CompilationUnit) parserJDT.createAST(null);
        isParsed = true;
        if(errorList.size() > 0){
            throw new ParseErrorsException("Parse error in " + filename, errorList);
            //throw new Exception("Parsing errors :" + Arrays.toString(errorList.toArray()));
        }

		ASTSrc.getInstance().setJDT(contextJDT);
    }

	public char[] getSource() {
		return source;
	}

	/**
     * Local Main to test  the current implementation
     * @param args          Not used
     * @throws Exception    If something goes wrong...here it is the description why it goes bad (hopefully)
     */
    public static void main(String[] args) throws Exception {
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/HelloWorld.java";
        Java2AST tmp = new Java2AST(base_path, VERSION.Java_7, true);
    }
}
