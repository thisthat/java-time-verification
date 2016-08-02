package PCFG.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG {
	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<Edge> E = new ArrayList<>();
	List<SyncEdge> ESync = new ArrayList<>();

	public PCFG() {
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
	public void addEdge(SyncEdge edge){
		this.ESync.add(edge);
	}

	public void addNode(SyncNode node) {
		this.syncNodes.add(node);
	}

	/**
	 * TODO: Implementing the optimization
	 */
	public void optimize(){}

	public String toGraphViz(boolean hideName){
		StringBuilder out = new StringBuilder();
		out.append("digraph {\nrankdir=LR;\ncompound=true;\n");
		for(Node v :  V){
			out.append(printNode(v, hideName) + ";\n");
		}
		int i = 0;
		for(SyncNode s : syncNodes){
			i++;
			out.append("subgraph cluster_" + s.getID() + " {\nnode [style=filled];\n");
			for(Node v : s.getNodes()){
				out.append(printNode(v, hideName) + ";\n");
			}
			out.append("label = \"sync block on " + s.getExpr() + "\";\ncolor=blue\n}\n");
		}
		for(IEdge e : E){
			out.append(printNode(e.getFrom(), hideName) + " -> " + printNode(e.getTo(), hideName) + ";\n");
		}
		for(SyncEdge sEdge : ESync){
			if(sEdge.getType() == SyncEdge.TYPE.SYNC_BLOCK){
				SyncNode from 	= ((SyncNode) sEdge.getFrom());
				SyncNode to 	= ((SyncNode) sEdge.getTo());
				Node f = from.getNodes().get(0);
				Node t = to.getNodes().get(0);
				out.append(printNode(f, hideName) + " -> " + printNode(t, hideName) + " [ltail=cluster_" + from.getID() + ",lhead=cluster_" + to.getID() + "];\n");
			}
		}
		out.append("}\n");
		return out.toString();
	}

	private static String printNode(INode v, boolean hideName) {
		if(hideName) {
			return "s" + v.getID();
		}
		return v.getName();
	}

	public SyncNode getSyncNodeByExpr(String expr, int line, String className){
		for(SyncNode s : syncNodes){
			if(
					s.getExpr().equals(expr) &&
					s.getClassName().equals(className) &&
					s.getLine() == line
			) {
				return s;
			}
		}
		return null;
	}
}
