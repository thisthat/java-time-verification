package structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class SyncNode implements INode {

	public static int _ID = 0;
	int id = 0;

	public SyncNode() {
		this.id = _ID++;
	}

	List<Node> nodes = new ArrayList<>();

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void add(Node node){
		this.nodes.add(node);
	}

	@Override
	public String getName() {
		return "sync_block";
	}

	@Override
	public int getID() {
		return id;
	}
}
