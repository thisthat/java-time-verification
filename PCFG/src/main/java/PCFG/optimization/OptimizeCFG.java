package PCFG.optimization;

import PCFG.structure.CFG;
import PCFG.structure.IOptimization;
import PCFG.structure.PCFG;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.Edge;
import PCFG.structure.node.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class OptimizeCFG implements IOptimization {
	@Override
	public PCFG optimize(PCFG pcfg) {
		for(CFG p : pcfg.getCFG()){
			handleCFG(p);
		}
		return pcfg;
	}

	private void handleCFG(CFG p) {
		if(p.getV().size() == 1){
			Node first = p.getV().get(0);
			//only one node. Create an empty init
			Node init = new Node("init","nop", Node.TYPE.USELESS, 0,0,-1);
			p.getV().add(init);
			Edge e = new Edge(init, first);
			p.getE().add(e);
			init.setStart(true);
			first.setStart(false);
			first.setEnd(true);
			if(first.getConstraint() != null){
				e.setConstraint(first.getConstraint());
			}
		}

		List<Edge> edges = new ArrayList<>();
		for(Edge e : p.getE()){
			//remove the edge out of a return
			if(!e.getFrom().getType().equals(Node.TYPE.RETURN)){
				edges.add(e);
			}
		}
		p.setE(edges);
		for(Node n : p.getAllNodes()){
			boolean f = false;
			for(Edge e : p.getE()){
				if(e.getFrom().equals(n)){
					f = true;
				}
			}
			n.setEnd(!f); //set end node only when there are no exit edges
			//move cnst to edge
			if(n.getConstraint() != null){
				for(Edge e : p.getE()){
					if(e.getFrom().equals(n)){
						e.setConstraint(n.getConstraint());
					}
				}
			}
		}

		for(AnonymClass a : p.getAnonNodes()){
			for(CFG c : a.getCFG()){
				handleCFG(c);
			}
		}
	}
}
