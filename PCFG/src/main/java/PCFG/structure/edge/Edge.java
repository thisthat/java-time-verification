package PCFG.structure.edge;

import PCFG.structure.node.Node;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Edge implements IEdge {
	Node from;
	Node to;
	String label;
	private Constraint constraint;

	public Edge(Node from, Node to) {
		this.from = from;
		this.to = to;
		this.label = "";
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public Constraint getConstraint() {
		return constraint;
	}
}
