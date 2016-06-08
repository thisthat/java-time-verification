package heuristic;

import IntermediateModel.interfaces.IASTStm;
import XALConversion.util.Pair;
import envirorment.Env;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * The Class should be inherit in order to specify new Strategies to find time constraint
 * It defines a list of where there are time constraint found.
 *
 */
public abstract class SearchTimeConstraint {
	protected int line;
	protected String lineSrc;
	protected List<Pair<Integer,String>> timeConstraint = new ArrayList<>();

	public abstract void next(IASTStm stm, Env env);

	protected void addConstraint(int line, String src){
		timeConstraint.add( new Pair<Integer, String>(line, src) );
	}

	protected List<Pair<Integer, String>> getTimeConstraint() {
		return timeConstraint;
	}
}
