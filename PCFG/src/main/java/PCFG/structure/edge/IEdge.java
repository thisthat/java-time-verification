package PCFG.structure.edge;

import PCFG.structure.ICFGElement;
import PCFG.structure.node.INode;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IEdge extends ICFGElement {
	void setLabel(String label);
	String getLabel();
	INode getFrom();
	INode getTo();

}
