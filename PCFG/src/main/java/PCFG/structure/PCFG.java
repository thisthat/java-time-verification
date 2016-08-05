package PCFG.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handle the Parallel Control Flow Graph representation.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG {

	List<SyncEdge> ESync = new ArrayList<>();
	List<CFG> processes = new ArrayList<>();

	public PCFG() {
	}

	/**
	 * Insert a synchronization edge.
	 * @param edge Edge to add.
	 */
	public void addSyncEdge(SyncEdge edge){
		this.ESync.add(edge);
	}

	/**
	 * Insert a {@link CFG}.
	 * Each process/thread has its own Control Flow Graph.
	 * @param cfg Control flow graph to add.
	 *
	 * @see CFG
	 */
	public void addCFG(CFG cfg){
		processes.add(cfg);
	}

	/**
	 * Get the list of {@link SyncNode} among all the CFGs.
	 * @return	list of {@link SyncNode} among all the CFGs.
	 */
	public List<SyncNode> getSyncNodes(){
		List<SyncNode> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getSyncNodes() );
		}
		return out;
	}

	/**
	 * Get list of {@link Node} among all the CFGs.
	 * @return list of {@link Node} among all the CFGs.
	 */
	public List<Node> getV() {
		List<Node> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getV() );
		}
		return out;
	}

	/**
	 * Get list of {@link Edge} among all the CFGs.
	 * @return list of {@link Edge} among all the CFGs.
	 */
	public List<Edge> getE() {
		List<Edge> out = new ArrayList<>();
		for(CFG cfg : processes){
			out.addAll( cfg.getE() );
		}
		return out;
	}

	/**
	 * Get list of {@link SyncEdge} in the PCFG.
	 * @return list of {@link SyncEdge} in the PCFG.
	 */
	public List<SyncEdge> getESync() {
		return ESync;
	}

	/**
	 * Get the list of CFGs in the PCFG.
	 * @return List of {@link CFG}
	 */
	public List<CFG> getProcesses() {
		return processes;
	}

	/**
	 * TODO: Implementing the optimization
	 */
	public void optimize(){}

	/**
	 * PrettyPrint the PCFG in the Graphviz syntax
	 * @param hideName	If the flag is set to true, the names are converted with a progressive number
	 * @return	Graphviz representation of the PCFG
	 */
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

	/**
	 * Return the name of the Node
	 * @param v			Node to print the name
	 * @param hideName	Flag to check if use real name or ids
	 * @return			Name of the node
	 */
	private static String printNode(INode v, boolean hideName) {
		if(hideName) {
			return "s" + v.getID();
		}
		return v.getName();
	}

	/**
	 * From an expression, retrieve the correlative synchronization node.
	 * @param expr			Src code of the expression
	 * @param line			Line number of the node
	 * @param className		Class of the node to whom belongs to.
	 * @return	The syncnode.
	 */
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
