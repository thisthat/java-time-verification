package parser.exception;

import parser.grammar.ParseError;

import java.util.List;

/**
 * Custom Exception to handle parse errors
 *
 * @author      Giovanni Liva (@thisthatDC)
 * @version     %I%, %G%
 */
public class ParseErrorsException extends Exception {

    List<ParseError> errors;

    public ParseErrorsException(){   }

    public ParseErrorsException(String msg){
        super(msg);
    }

    /**
     * Constructor of the Exception
     * @param msg           The message to show
     * @param listErrors    List of errors collected while parsing
     */
    public ParseErrorsException(String msg, List<ParseError> listErrors){
        super(msg);
        errors = listErrors;
    }

    /**
     * Getter method to manually handle the list of errors collected while parsing
     * @return the error list
     */

    public List<ParseError> getParsingErrors(){
        return errors;
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString(){
        String out = super.getMessage() + "\n";
        for(int i = 0; i < errors.size(); i++){
            ParseError tmp = errors.get(i);
            out += "@" + tmp.getLastErrorLine();
            out += ":" + tmp.getLastErrorChar();
            out += " " + tmp.getLastErrorMsg();
            out += "\n";
        }
        return out;
    }
}
