package PCFG.optimization;

import PCFG.structure.CFG;
import PCFG.structure.IOptimization;
import PCFG.structure.PCFG;
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
			List<Edge> edges = new ArrayList<>();
			for(Edge e : p.getE()){
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


		}
		return pcfg;
	}
}
