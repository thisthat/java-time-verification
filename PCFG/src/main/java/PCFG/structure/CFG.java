package PCFG.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CFG {
	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<Edge> E = new ArrayList<>();
	String name;

	public CFG(String name) {
		this.name = name;
	}

	public List<Node> getV() {
		return V;
	}

	public List<Edge> getE() {
		return E;
	}

	public void addNode(Node node){
		this.V.add(node);
	}

	public void addEdge(Edge edge){
		this.E.add(edge);
	}

	public void addNode(SyncNode node) {
		this.syncNodes.add(node);
	}

	public List<SyncNode> getSyncNodes() {
		return syncNodes;
	}

	public String getName() {
		return name;
	}
}
