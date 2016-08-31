package PCFG.structure.edge;

import PCFG.structure.CFG;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.node.INode;
import PCFG.structure.node.Node;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnonymEdge implements IEdge {
	INode from;
	INode to;
	String label;

	public AnonymEdge(INode from, INode to) {
		this.from = from;
		this.to = to;
		this.label = "";
	}

	public INode getFrom() {
		return from;
	}

	public INode getTo() {
		return to;
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
		if(this.getTo() instanceof AnonymClass && ((AnonymClass) this.getTo()).getCFG().size() > 0){
			AnonymClass ac = (AnonymClass)this.getTo();
			CFG tmp = ac.getCFG().get(0);
			if(tmp.getV().size() > 0) {
				out.append(this.getFrom().toGraphViz(hideName) + " -> " + tmp.getV().get(0).toGraphViz(hideName));
				out.append("[ lhead = \"" + AnonymClass._CLUSTER_NAME + this.getTo().getID() + "\" ]");
				out.append(";\n");
			}
		}
		return out.toString();
	}
}
