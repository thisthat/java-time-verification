package intermediateModel.interfaces;


import com.google.common.annotations.Beta;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.javatuples.Triplet;
import parser.ASTSrc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The following class is the basic of our IM.
 * It provides the set of methods that each member of the IM has to have.
 * Although it is abstract, it does not have any abstract methods. It is abstract just to prevent
 * any initialization of the following general object without any concrete semantic for a Java program.
 * <br />
 * It provides all the basic structure to have pointer to code, the code itself, knowledge about if is it time critical
 * and store annotations for latter usage (e.g. Apply Heuristics).
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public abstract class IASTStm implements IASTVisitor{
	/**
	 * @deprecated  we uses the type of the class itself to identify the semantic of the object
	 */
	@Deprecated
	public enum Type {
		Break,
		Continue,
		For,
		Foreach,
		If,
		RE,
		Return,
		Switch,
		Try
	}

	/**
	 * Enum about the different visibility access level that Java offers
	 */
	public enum Visibility {
		PUBLIC,
		PROTECT,
		ABSTRACT,
		FINAL,
		PRIVATE,
		STRICTFP
	}

	protected int start;
	protected int end;
	protected Token startToken = null;
	protected Token endToken = null;
	protected String code = "";
	protected int line;
	protected Triplet<Integer,String, Class> constraint = null;
	@Beta protected List<String> annotations = new ArrayList<>();
	private boolean isTimeCritical = false;
	/**
	 * Retrive the time constraint of the current Node
	 * @return the information of the time constraint with &lt;line, message, class that detected the constraint&gt;
	 */
	public Triplet<Integer,String, Class> getConstraint(){
		return constraint;
	}

	/**
	 * Attach a time constraint to the node
	 * @param line			line of code where it is located
	 * @param msg			message with information about the constraint
	 * @param heuristic		class that detected the constraint
	 */
	public void addConstraint(Integer line, String msg, Class heuristic){
		isTimeCritical = true;
		constraint = new Triplet<>(line,msg, heuristic);
	}

	/**
	 * Get information about if it is a time relevant or not
	 * @return	true if is time related
	 */
	public boolean isTimeCritical(){
		return this.isTimeCritical;
	}

	/**
	 * Set if it is time critical or not
	 * @param timeCritical boolean variable
	 */
	public void setTimeCritical(boolean timeCritical) {
		isTimeCritical = timeCritical;
	}

	/**
	 * Set the source code of the node starting from a char array that contains the whole Java file.
	 * @param source byte representation of the Java file
	 */
	protected void calculateSourceCode(char[] source){
		code = Arrays.copyOfRange(source, start, end).toString();
	}

	/**
	 * Calculate the source code of the node.
	 * It tries to use the ANTLRv4 Token data structure to retrieve the source code.
	 * If they are not available it uses the {@link ASTSrc} class.
	 */
	protected void calculateSourceCode(){
		if(startToken == null || endToken == null){
			ASTSrc instance = ASTSrc.getInstance();
			char[] source = instance.source;
			line = instance.getLine(start) + 1;
			code = new String(Arrays.copyOfRange(source, start, end));
			return;
		}
		code = startToken.getInputStream().getText(new Interval(startToken.getStartIndex(), endToken.getStopIndex()));
		line = startToken.getLine();
		//code = new String(Arrays.copyOfRange(source, start, end));
	}

	/**
	 * Static method to help the handle of source code extraction form ANTLRv4 Token structure
	 * @param start start token
	 * @param end   end token
	 * @return		the source code associated w/ the tokens
	 */
	public static String getSrcFromToken(Token start, Token end){
		return start.getInputStream().getText(new Interval(start.getStartIndex(), end.getStopIndex()));
	}

	/**
	 * It returns the source code of the node.
	 * It it is not yet available, the method extracts it from the file.
	 * @param source	Char array that represent the file from where extract the source code
	 * @return			The source code of the node
	 */
	protected String getCode(char[] source){
		if(code == null || code.equals("")){
			calculateSourceCode(source);
		}
		return code;
	}

	/**
	 * It returns the source code of the node.
	 * It it is not yet available, the method call {@link #calculateSourceCode()}.
	 * @return	The source code of the node
	 */
	public String getCode(){
		if(code == null || code.equals("")){
			calculateSourceCode();
		}
		return code;
	}

	/**
	 * Get the line in the file of the node
	 * @return the line number
	 */
	public int getLine(){
		return line;
	}

	/**
	 * <b>BETA</b>.
	 * Add annotation to the node
	 * @param annotation String that contains the annotation to add
	 */
	@Beta
	public void addAnnotation(String annotation){
		annotations.add(annotation);
	}

	/**
	 * <b>BETA</b>.
	 * Get all the annotation of the node
	 * @return	The list of annotations
	 */
	@Beta
	public List<String> getAnnotations(){
		return annotations;
	}

	/**
	 * <b>Beta</b>.
	 * Search if an annotation is already present in the list of the node.
	 * @param annotation	The annotation to search
	 * @return				True if it already exists.
	 */
	@Beta
	public boolean existsAnnotation(String annotation){
		return annotations.stream().anyMatch(a -> annotation.equals(a));
	}

	/**
	 * Constructor that uses ANTLRv4 Token structure
	 * @param start	Start token
	 * @param end	End token
	 */
	protected IASTStm(Token start, Token end){
		this.start = start.getStartIndex();
		this.end = end.getStopIndex();
		this.startToken = start;
		this.endToken = end;
		calculateSourceCode();
	}

	/**
	 * Constructor that uses general start and end position in the file of the node
	 * @param start	start position of the node
	 * @param end	end position of the node
	 */
	protected IASTStm(int start, int end){
		this.start = start;
		this.end = end;
		calculateSourceCode();
	}

	/**
	 * Get the start index
	 * @return start position
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Get the end index
	 * @return end position
	 */
	public int getEnd() {
		return end;
	}
}
