package structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG {
	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<IEdge> E = new ArrayList<>();

	public PCFG() {
	}

	public List<Node> getV() {
		return V;
	}

	public List<IEdge> getE() {
		return E;
	}

	public void addNode(Node node){
		this.V.add(node);
	}

	public void addEdge(IEdge edge){
		this.E.add(edge);
	}

	public void addNode(SyncNode node) {
		this.syncNodes.add(node);
	}

	/**
	 * TODO: Implementing the optimization
	 */
	public void optimize(){}
}
