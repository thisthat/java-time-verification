package IntermediateModelHelper.heuristic;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import IntermediateModelHelper.envirorment.Env;
import XALConversion.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * The Class should be inherit in order to specify new Strategies to find time constraint.
 * It defines a list of where there are time constraint found.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 */
public abstract class SearchTimeConstraint {

	/**
	 * Stores the list of {@link Pair} of time instruction the heurisitc found.
	 * It saves line and code of the time constraint.
	 */
	protected List<Pair<Integer,String>> timeConstraint = new ArrayList<>();

	/**
	 * It used to accept a Statement
	 * @param stm	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
	public abstract void next(IASTStm stm, Env env);
	/**
	 * It used to accept an Expression
	 * @param expr	Expression to process
	 * @param env	Envirorment visible to that statement
	 */
	public abstract void next(IASTRE expr, Env env);

	/**
	 * Add a constraint to the list
	 * @param stm	The instruction to add to the list
	 */
	protected void addConstraint(IASTStm stm){
		timeConstraint.add( new Pair<Integer, String>(stm.getLine(), stm.getCode()) );
	}

	/**
	 * Add an line of code as time constraint.
	 * @param line	The number of line in the code
	 * @param src  	The source code
	 */
	protected void addConstraint(int line, String src){
		timeConstraint.add( new Pair<Integer, String>(line, src) );
	}

	/**
	 * Getter.
	 * @return The list of constraint.
	 */
	public List<Pair<Integer, String>> getTimeConstraint() {
		return timeConstraint;
	}


}
