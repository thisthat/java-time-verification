package intermediateModel.interfaces;

import intermediateModel.structure.ASTRE;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTVar {
	String getType();
	String getTypeNoArray();
	String getName();
	ASTRE getExpr();
	int getLine();
	boolean isTimeCritical();
	void setTimeCritical(boolean timeCritical);
	boolean equals(Object o);
	public String getTypePointed();
}
