package structure;

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
}
