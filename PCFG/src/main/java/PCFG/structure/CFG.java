package PCFG.structure;

import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.AnonymEdge;
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
	public static final String _CLUSTER_NAME = "cluster_cfg_";
	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<AnonymClass> anonNodes = new ArrayList<>();
	List<Edge> E = new ArrayList<>();
	List<AnonymEdge> anonEdge = new ArrayList<>();
	String name;
	int id;

	public CFG(String name) {
		this.name = name;
		this.id = ID++;
	}

	public List<Node> getV() {
		return V;
	}

	public List<Node> getAllNodes(){
		List<Node> nodes = new ArrayList<>();
		nodes.addAll(V);
		for(AnonymClass c : anonNodes){
			nodes.addAll( c.AllNodes() );
		}
		return nodes;
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

	public void addEdge(AnonymEdge edge){
		this.anonEdge.add(edge);
	}

	public void addNode(SyncNode node) {
		this.syncNodes.add(node);
	}

	public void addNode(AnonymClass node) { this.anonNodes.add(node); }

	public List<SyncNode> getSyncNodes() {
		List<SyncNode> nodes = new ArrayList<>();
		nodes.addAll(syncNodes);
		for(AnonymClass c : anonNodes){
			nodes.addAll( c.getSyncNodes() );
		}
		return nodes;
	}

	private List<SyncNode> getSyncNodesNoSubClass() {
		return syncNodes;
	}

	public String getName() {
		return name;
	}

	public List<AnonymClass> getAnonNodes() {
		return anonNodes;
	}

	public List<AnonymEdge> getAnonEdge() {
		return anonEdge;
	}

	@Override
	public String toGraphViz(boolean hideName) {
		StringBuilder out = new StringBuilder();
		out.append("\tsubgraph " + _CLUSTER_NAME + this.id + " {\n\t\tnode [style=filled];\n");
		for(Node v :  this.getV()){
			out.append("\t" + v.toGraphViz(hideName) + ";\n");
		}
		for(SyncNode s : this.getSyncNodesNoSubClass()){
			out.append("\t" + s.toGraphViz(hideName));
		}
		for(IEdge e : this.getE()){
			out.append("\t" + e.toGraphViz(hideName));
		}
		for(AnonymClass a : this.getAnonNodes()){
			out.append("\t" + a.toGraphViz(hideName));
		}
		for(AnonymEdge ae : this.getAnonEdge()){
			out.append("\t" + ae.toGraphViz(hideName));
		}
		out.append("\t\tlabel = \"" + this.getName() + "\";\n\t\tcolor=green\n\t}\n");
		return out.toString();
	}




}
