package PCFG.structure;

import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.AnonymEdge;
import PCFG.structure.edge.Edge;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CFG {

	private static int ID = 0;
	public static final String _CLUSTER_NAME = "cluster_cfg_";
	List<Node> V = new ArrayList<>();
	List<SyncNode> syncNodes = new ArrayList<>();
	List<AnonymClass> anonNodes = new ArrayList<>();
	List<Edge> E = new ArrayList<>();
	List<AnonymEdge> anonEdge = new ArrayList<>();
	String name;
	int id;
	int hashcode;

	public CFG(String name, int hashcode) {
		this.name = name;
		this.id = ID++;
		this.hashcode = hashcode;
	}

	public int getID() {
		return this.id;
	}

	public int getHashcode() {
		return hashcode;
	}

	public List<Node> getV() {
		return V;
	}

	void setV(List<Node> V){
		this.V = V;
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

	public List<SyncNode> getSyncNodesNoSubClass() {
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

	public void setStartEnd(){
		if(this.getV().size() > 0) {
			this.getV().get(0).setStart(true);
			this.getV().get(this.getV().size() - 1).setEnd(true);
		}
		for(AnonymClass c : this.getAnonNodes()){
			c.setStartEnd();
		}
	}

}
