package structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class PCFG {
	List<INode> V = new ArrayList<>();
	List<IEdge> E = new ArrayList<>();

	public PCFG() {
	}

	public List<INode> getV() {
		return V;
	}

	public List<IEdge> getE() {
		return E;
	}

	public void addNode(INode node){
		this.V.add(node);
	}

	public void addEdge(IEdge edge){
		this.E.add(edge);
	}

}
