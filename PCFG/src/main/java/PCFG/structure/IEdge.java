package PCFG.structure;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IEdge {
	void setLabel(String label);
	String getLabel();
	INode getFrom();
	INode getTo();

}
