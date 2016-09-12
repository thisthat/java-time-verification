package PCFG.converter;

import PCFG.structure.CFG;
import PCFG.structure.PCFG;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.AnonymEdge;
import PCFG.structure.edge.Edge;
import PCFG.structure.edge.IEdge;
import PCFG.structure.edge.SyncEdge;
import PCFG.structure.node.INode;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;
import XAL.XALStructure.XALAddState;
import XAL.XALStructure.exception.XALMalformedException;
import XAL.XALStructure.items.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ToXAL implements IConverter {

	XALDocument doc = null;
	XALAddState aut = null;
	XALAutomaton lastAutomaton = null;
	@Override
	public String convert(PCFG pcfg) {
		return getXAL(pcfg).toString();
	}

	public XALDocument getXAL(PCFG pcfg) {
		String name = "";
		for(CFG c : pcfg.getCFG()){
			name +=  c.getName() + "_";
		}
		XALDocument document = new XALDocument(name);
		this.doc = document;
		for(CFG c : pcfg.getCFG()){
			getXAL(c);
		}
		for(SyncEdge sEdge : pcfg.getESync()){

		}
		return document;
	}


	private void getXAL(CFG cfg) {
		XALAutomaton automa = new XALAutomaton(cfg.getName());

		for(Node v : cfg.getV()){
			XALState s = new XALState(v.getName());
			automa.addState(s);
			if(v.getConstraint() != null){

			}
			if(v.isStart()){
				try {
					automa.setInitialState(s);
				} catch (XALMalformedException e) {
					e.printStackTrace();
				}
			}
			if(v.isEnd()){
				automa.addFinalState(s);
			}
		}
		for(SyncNode s : cfg.getSyncNodesNoSubClass()){
			getXAL(s);
		}
		for(IEdge e : cfg.getE()){
			getXAL(e);
		}
		for(AnonymClass a : cfg.getAnonNodes()){
			getXAL(a);
		}
		for(AnonymEdge ae : cfg.getAnonEdge()){
			getXAL(ae);
		}
		//automa.addFinalState();
		doc.addAutomaton(automa);
	}

	private void getXAL(AnonymClass a) {
		String className = a.getName();
		String _id = "definition_of_" + className;
		XALAutomaton automa = new XALAutomaton(_id);

	}

	private void getXAL(INode s) {
		if(s instanceof Node){
			getXAL((Node)s);
		} else {
			getXAL((SyncNode)s);
		}
	}

	private void getXAL(Node v) {
		XALState s = new XALState(v.getName());
		s.setNumericID(v.getID());
		aut.addState(s);
	}

	private void getXAL(SyncNode sv) {
		XALSync syncNode = new XALSync(sv.getExpr(), aut);
		syncNode.setNumericID(sv.getID());
		aut.addState(syncNode);
		XALAddState bck = aut;
		aut = syncNode;
		for(Node v : sv.getNodes()){
			getXAL(v);
		}
		aut = bck;
	}

	private void getXAL(IEdge e) {
		if(e instanceof Edge){
			getXAL((Edge)e);
		} else if(e instanceof AnonymEdge){
			getXAL((AnonymEdge)e);
		} else {
			getXAL((SyncEdge)e);
		}
	}

	private void getXAL(Edge e) {
		Node from = e.getFrom();
		Node to = e.getTo();
		XALState f = doc.getNodeFromNumericID(from.getID());
		XALState t = doc.getNodeFromNumericID(to.getID());
		lastAutomaton.addTransition(new XALTransition(f,t));
	}

	private void getXAL(AnonymEdge e) {
		XALState f = doc.getNodeFromNumericID(e.getFrom().getID());
		String className = e.getTo().getName();
		String _id = "definition_of_" + className;
		f.setNameMetric(_id);
		XALMetric action = new XALMetric(_id, XALProduction.ProductionType.automaton, className, doc.getFilename(), new XALEnumeration() );
		lastAutomaton.getActionPool().addProduction(action);
	}

	private void getXAL(SyncEdge e) {
		if(e.getType() == SyncEdge.TYPE.SYNC_BLOCK){
			SyncNode from = (SyncNode) e.getFrom();
			SyncNode to = (SyncNode) e.getTo();
			XALState f = doc.getSyncNodeFromNumericID(from.getID());
			XALState t = doc.getSyncNodeFromNumericID(to.getID());
			lastAutomaton.addTransition(new XALTransition(f,t));
		} else {
			Node from = (Node) e.getFrom();
			Node to = (Node) e.getTo();
			XALState f = doc.getNodeFromNumericID(from.getID());
			XALState t = doc.getNodeFromNumericID(to.getID());
			lastAutomaton.addTransition(new XALTransition(f,t));
		}
	}
}
