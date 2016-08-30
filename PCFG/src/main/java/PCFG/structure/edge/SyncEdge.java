package PCFG.structure.edge;

import PCFG.structure.PCFG;
import PCFG.structure.node.INode;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SyncEdge implements IEdge {

	public class MalformedSyncEdge extends Exception {
		public MalformedSyncEdge(String message) {
			super(message + " :: [" + from + " -> " + to + "]");
		}
	}

	public enum TYPE {
		SYNC_BLOCK,
		SYNC_NODE
	}

	INode from;
	INode to;
	TYPE type;
	String label;

	public SyncEdge(INode from, INode to) throws MalformedSyncEdge {
		this.from = from;
		this.to = to;
		if(from == null || to == null){
			//TODO fix this null that can appears
			System.out.println("Breakpoint");
		}
		if(!from.getClass().equals(to.getClass())){
			throw new MalformedSyncEdge("From and To nodes are not of the same kind");
		}
		type = from.getClass() == Node.class ? TYPE.SYNC_NODE : TYPE.SYNC_BLOCK;
	}

	public INode getFrom() {
		return from;
	}

	public INode getTo() {
		return to;
	}

	public TYPE getType() {
		return type;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String toGraphViz(boolean hideName) {
		StringBuilder out = new StringBuilder();

		if(this.getType() == SyncEdge.TYPE.SYNC_BLOCK){
			SyncNode from 	= ((SyncNode) this.getFrom());
			SyncNode to 	= ((SyncNode) this.getTo());
			Node f = from.getNodes().get(0);
			Node t = to.getNodes().get(0);
			out.append(f.toGraphViz(hideName) + " -> " + t.toGraphViz(hideName) + " [ltail=cluster_0" + from.getID() + ",lhead=cluster_sync" + to.getID() + "];\n");
		} else {
			Node from = (Node) this.getFrom();
			Node to   = (Node) this.getTo();
			out.append(from.toGraphViz(hideName) + " -> " + to.toGraphViz(hideName) + "[color=red,penwidth=1.0];\n");
		}

		return out.toString();
	}
}
