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

	public String toGraphViz(boolean hideName){
		StringBuilder out = new StringBuilder();
		out.append("digraph {\nrankdir=LR;\n");
		if(hideName){
			for(Node v :  V){
				out.append("s" + v.getID() + ";\n");
			}
			int i = 0;
			for(SyncNode s : syncNodes){
				i++;
				out.append("subgraph cluster_1 {\nnode [style=filled];\n");
				for(Node v : s.getNodes()){
					out.append("s" + v.getID() + ";\n");
				}
				out.append("label = \"sync block " + i + "\";\ncolor=blue\n}\n");
			}
			for(IEdge e : E){
				out.append("s" + e.getFrom().getID() + " -> " + "s" + e.getTo().getID() + ";\n");
			}
		}
		else {
			for(Node v :  V){
				out.append(v.getName() + ";\n");
			}
			int i = 0;
			for(SyncNode s : syncNodes){
				i++;
				out.append("subgraph cluster_1 {\nnode [style=filled];\n");
				for(Node v : s.getNodes()){
					out.append(v.getName() + ";\n");
				}
				out.append("label = \"sync block " + i + "\";\ncolor=blue\n}\n");
			}
			for(IEdge e : E){
				out.append(e.getFrom().getName() + " -> " + e.getTo().getName() + ";\n");
			}
		}
		out.append("}\n");
		return out.toString();
	}
}
