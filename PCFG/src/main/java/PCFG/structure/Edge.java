package PCFG.structure;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Edge implements IEdge{
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
}
