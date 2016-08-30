package PCFG.structure.edge;

import PCFG.structure.PCFG;
import PCFG.structure.node.Node;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Edge implements IEdge {
	Node from;
	Node to;
	String label;

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

	@Override
	public String toGraphViz(boolean hideName) {
		StringBuilder out = new StringBuilder();
		out.append(this.getFrom().toGraphViz(hideName) + " -> " + this.getTo().toGraphViz(hideName));
		out.append("[ label = \"" + this.getLabel() + "\" ]");
		out.append(";\n");
		return out.toString();
	}
}
