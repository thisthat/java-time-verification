package PCFG.optimization;

import PCFG.structure.CFG;
import PCFG.structure.IOptimization;
import PCFG.structure.PCFG;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.Edge;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;
import intermediateModelHelper.heuristic.definition.AnnotatedTypes;
import intermediateModelHelper.heuristic.definition.TimeoutResources;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class OptimizeTimeAutomata implements IOptimization {


	@Override
	public PCFG optimize(PCFG pcfg) {
		for(CFG p : pcfg.getCFG()){
			handleCFG(p);
		}
		return pcfg;
	}

	private void handleCFG(CFG c) {
		// annotate reset of variables
		handleNodes(c.getV());
		handleEdges(c.getE());
		for(AnonymClass a : c.getAnonNodes()){
			for(CFG ac : a.getCFG()){
				handleCFG(ac);
			}
		}
	}

	private void handleNodes(List<Node> V){
		for(Node n : V){
			if(n.getConstraint() != null) {
				if(n.getConstraint().isCategory(AnnotatedTypes.class)){
					//n.setResetClock(true);
					n.getConstraint().setValue("t <= " + n.getConstraint().getValue());
				}
				if(n.getConstraint().isCategory(TimeoutResources.class)){

				}
			}
		}
	}

	private void handleEdges(List<Edge> E){
		for(Edge e : E){
			Node n = e.getTo();
			if(n.getConstraint() != null) {
				if(n.getConstraint().isCategory(AnnotatedTypes.class)){
					e.getFrom().setResetClock(true);
					//n.getConstraint().setValue("t <= " + n.getConstraint().getValue());
				}
				if(n.getConstraint().isCategory(TimeoutResources.class)){

				}
			}
		}
	}


}
