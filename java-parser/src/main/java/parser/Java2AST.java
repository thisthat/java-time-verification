package parser;

import parser.exception.ParseErrorsException;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parser.grammar.*;


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

    /**
     * Getter of the AST
     * @return the AST of the source file
     */
    public ParserRuleContext getContext() {
        return context;
    }

    private ParserRuleContext context;

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
    public Java2AST(String filename, boolean parse) throws IOException, ParseErrorsException {
        this.filename = filename;
        initParser();
        if(parse){
            convertToAST();
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
    public void convertToAST() throws ParseErrorsException {
        errorList.clear();
        context = parser.compilationUnit();
        isParsed = true;
        if(errorList.size() > 0){
            throw new ParseErrorsException("Parse error in " + filename, errorList);
            //throw new Exception("Parsing errors :" + Arrays.toString(errorList.toArray()));
        }
    }


    /**
     * Local Main to test  the current implementation
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/HelloWorld.java";
        Java2AST tmp = new Java2AST(base_path, true);
    }
}
