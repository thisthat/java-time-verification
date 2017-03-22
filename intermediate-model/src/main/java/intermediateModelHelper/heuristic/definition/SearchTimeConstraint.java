package intermediateModelHelper.heuristic.definition;

import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTConstructor;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModelHelper.envirorment.Env;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;
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
	 * It saves:
	 * <ul>
	 *     <li>Message</li>
	 *     <li>Node of IM</li>
	 *     <li>Type of Heuristic that found the constraint</li>
	 * </ul>
	 */
	protected List<Constraint> timeConstraint = new ArrayList<>();

	/**
	 * It used to set up internal resources
	 */
	public abstract void setup(ASTClass c);

	/**
	 * It used to accept a Statement
	 * @param stm	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
	public abstract void next(ASTRE stm, Env env);

	/**
	 * It used to accept a new Method Definition
	 * @param method	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
	public abstract void nextMethod(ASTMethod method, Env env);

	/**
	 * It used to accept a new Constructor Definition
	 * @param method	Statement to process
	 * @param env	Envirorment visible to that statement
	 */
	public abstract void nextConstructor(ASTConstructor method, Env env);

	/**
	 * Add a constraint to the list
	 * @param message Message to store with the time constraint
	 * @param stm	The instruction to add to the list
	 */
	protected void addConstraint(String message, IASTStm stm){
		Constraint elm = new Constraint(stm, getClass(), message, stm.getLine());
		if(!timeConstraint.contains(elm))
			timeConstraint.add( elm );
		stm.addConstraint( elm );
	}

	/**
	 * Getter.
	 * @return The list of constraint.
	 */
	public List<Constraint> getTimeConstraint() {
		return timeConstraint;
	}



}
