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
	private String expr;
	int line;
	String className;

	public SyncNode(String expr, int line, String className) {
		this.id = _ID++;
		this.expr = expr;
		this.line = line;
		this.className = className;
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

	public String getExpr() {
		return expr;
	}

	public int getLine() {
		return line;
	}

	public String getClassName() {
		return className;
	}
}
