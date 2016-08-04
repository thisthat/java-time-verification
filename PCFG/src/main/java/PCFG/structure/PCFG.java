package PCFG.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG {

	List<SyncEdge> ESync = new ArrayList<>();
	List<CFG> processes = new ArrayList<>();

	public PCFG() {
	}


	public void addSyncEdge(SyncEdge edge){
		this.ESync.add(edge);
	}

	public void addCFG(CFG cfg){
		processes.add(cfg);
	}

	public List<SyncNode> getSyncNodes(){
		List<SyncNode> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getSyncNodes() );
		}
		return out;
	}

	public List<Node> getV() {
		List<Node> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getV() );
		}
		return out;
	}
	public List<Edge> getE() {
		List<Edge> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getE() );
		}
		return out;
	}
	public List<SyncEdge> getESync() {
		return ESync;
	}

	public List<CFG> getProcesses() {
		return processes;
	}

	/**
	 * TODO: Implementing the optimization
	 */
	public void optimize(){}

	public String toGraphViz(boolean hideName){
		StringBuilder out = new StringBuilder();
		out.append("digraph {\nrankdir=TD;\ncompound=true;\n");

		int i = 0;
		for(CFG process : processes){
			out.append("subgraph cluster_" + i++ + " {\nnode [style=filled];\n");
			for(Node v :  process.getV()){
				out.append(printNode(v, hideName) + ";\n");
			}
			for(SyncNode s : process.getSyncNodes()){
				out.append("\tsubgraph cluster_0" + s.getID() + " {\n\tnode [style=filled];\n");
				for(Node v : s.getNodes()){
					out.append("\t" + printNode(v, hideName) + ";\n");
				}
				out.append("\tlabel = \"sync block on " + s.getExpr() + "\";\n\tcolor=blue\n}\n");
			}
			for(IEdge e : process.getE()){
				out.append(printNode(e.getFrom(), hideName) + " -> " + printNode(e.getTo(), hideName));
				out.append("[ label = \"" + e.getLabel() + "\" ]");
				out.append(";\n");
			}
			out.append("label = \"" + process.getName() + "\";\n\tcolor=green\n}\n");
		}

		for(SyncEdge sEdge : ESync){
			if(sEdge.getType() == SyncEdge.TYPE.SYNC_BLOCK){
				SyncNode from 	= ((SyncNode) sEdge.getFrom());
				SyncNode to 	= ((SyncNode) sEdge.getTo());
				Node f = from.getNodes().get(0);
				Node t = to.getNodes().get(0);
				out.append(printNode(f, hideName) + " -> " + printNode(t, hideName) + " [ltail=cluster_0" + from.getID() + ",lhead=cluster_0" + to.getID() + "];\n");
			} else {
				Node from = (Node) sEdge.getFrom();
				Node to   = (Node) sEdge.getTo();
				out.append(printNode(from, hideName) + " -> " + printNode(to, hideName) + "[color=red,penwidth=1.0];\n");
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
		for(SyncNode s : this.getSyncNodes()){
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
