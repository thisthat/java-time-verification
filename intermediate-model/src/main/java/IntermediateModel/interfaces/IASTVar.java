package intermediateModel.interfaces;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTVar {
	String getType();
	String getName();
	boolean isTimeCritical();
	void setTimeCritical(boolean timeCritical);
}
