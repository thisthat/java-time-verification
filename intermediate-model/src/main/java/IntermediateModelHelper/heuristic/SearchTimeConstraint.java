package IntermediateModelHelper.heuristic;

import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import IntermediateModelHelper.envirorment.Env;
import org.javatuples.Triplet;

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
	 * Stores the list of {@link Triplet} of time instruction the heurisitc found.
	 * It saves line and code of the time constraint.
	 */
	protected List<Triplet<Integer,String,Class>> timeConstraint = new ArrayList<>();

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
		timeConstraint.add( new Triplet<>(stm.getLine(), stm.getCode(), getClass()) );
	}

	/**
	 * Add an line of code as time constraint.
	 * @param line	The number of line in the code
	 * @param src  	The source code
	 */
	protected void addConstraint(int line, String src){
		timeConstraint.add( new Triplet<>(line, src, getClass()) );
	}

	/**
	 * Getter.
	 * @return The list of constraint.
	 */
	public List<Triplet<Integer, String, Class>> getTimeConstraint() {
		return timeConstraint;
	}


}
