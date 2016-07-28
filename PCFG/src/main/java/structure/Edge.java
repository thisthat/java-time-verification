package structure;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
class Edge {
	Node from;
	Node to;

	public Edge(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}
}
