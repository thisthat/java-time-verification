package PCFG.structure;

import PCFG.structure.edge.Edge;
import PCFG.structure.edge.SyncEdge;
import PCFG.structure.edge.IEdge;
import PCFG.structure.node.INode;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handle the Parallel Control Flow Graph representation.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG implements ICFGElement, IHasCFG {

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
	public List<CFG> getCFG() {
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
	//TODO rewrite this ugly method
	public String toGraphViz(boolean hideName){
		StringBuilder out = new StringBuilder();
		out.append("digraph {\n\trankdir=TD;\n\tcompound=true;\n");

		int i = 0;
		for(CFG process : processes){
			out.append(process.toGraphViz(hideName));
		}
		for(SyncEdge sEdge : ESync){
			out.append(sEdge.toGraphViz(hideName));
		}
		out.append("}\n");
		return out.toString();
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

	public INode getNodeInBound(int start, int end){
		return null;
	}
}
