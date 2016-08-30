package PCFG.structure;

import PCFG.structure.edge.Edge;
import PCFG.structure.edge.IEdge;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CFG implements ICFGElement {

	private static int ID = 0;

	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<Edge> E = new ArrayList<>();
	String name;
	int id;

	public CFG(String name) {
		this.name = name;
		this.id = ID++;
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

	@Override
	public String toGraphViz(boolean hideName) {
		StringBuilder out = new StringBuilder();
		out.append("\tsubgraph cluster_cfg_" + this.id + " {\n\t\tnode [style=filled];\n");
		for(Node v :  this.getV()){
			out.append("\t" + v.toGraphViz(hideName) + ";\n");
		}
		for(SyncNode s : this.getSyncNodes()){
			out.append("\t" + s.toGraphViz(hideName));
		}
		for(IEdge e : this.getE()){
			out.append("\t" + e.toGraphViz(hideName));
		}
		out.append("\t\tlabel = \"" + this.getName() + "\";\n\t\tcolor=green\n\t}\n");
		return out.toString();
	}
}
