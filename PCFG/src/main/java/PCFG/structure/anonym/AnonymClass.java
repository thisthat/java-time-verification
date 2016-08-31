package PCFG.structure.anonym;

import PCFG.structure.CFG;
import PCFG.structure.IHasCFG;
import PCFG.structure.edge.IEdge;
import PCFG.structure.node.INode;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;

import java.util.ArrayList;
import java.util.List;

/**
 * With this class we represent the PCFG for an anonymous class.
 * We have a CFG for each method.
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class AnonymClass implements INode, IHasCFG {

	private static int ID = 0;
	private int id;
	private List<CFG> methods = new ArrayList<>();
	public static final String _CLUSTER_NAME = "cluster_anonymcfg_";

	public AnonymClass() {
		this.id = ID++;
	}

	@Override
	public String getName() {
		return "anonymousClass_" + this.id;
	}

	@Override
	public int getID() {
		return this.id;
	}

	@Override
	public String toGraphViz(boolean hideName) {
		StringBuilder out = new StringBuilder();
		out.append("\tsubgraph " + _CLUSTER_NAME +  this.id + " {\n\t\tnode [style=filled];\n");
		for(CFG m : methods){
			out.append(m.toGraphViz(hideName));
		}
		out.append("\t\tlabel = \"" + this.getName() + "\";\n\t\tcolor=red\n\t}\n");
		return out.toString();
	}

	@Override
	public void addCFG(CFG cfg) {
		this.methods.add(cfg);
	}

	@Override
	public List<CFG> getCFG() {
		return this.methods;
	}
}
