package PCFG.converter;

import PCFG.optimization.OptimizeForXAL;
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
		String name = "";
		for(CFG c : pcfg.getCFG()){
			name +=  c.getName() + "_";
		}
		return getXAL(pcfg,name).toString();
	}

	public XALDocument getXAL(PCFG pcfg, String filename) {
		pcfg = pcfg.optimize( new OptimizeForXAL() );
		XALDocument document = new XALDocument(filename);
		this.doc = document;
		for(CFG c : pcfg.getCFG()){
			getXAL(c);
		}
		/*for(SyncEdge sEdge : pcfg.getESync()){

		}*/
		return document;
	}


	private void getXAL(CFG cfg){
		getXAL(cfg, cfg.getName());
	}

	private void getXAL(CFG cfg, String name) {
		XALAutomaton automa = new XALAutomaton(name);
		doc.addAutomaton(automa);
		lastAutomaton = automa;
		aut = automa;
		for(Node v : cfg.getV()){
			getXAL(v);
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
		/*
		for(AnonymEdge ae : cfg.getAnonEdge()){
			getXAL(ae);
		}*/
		//automa.addFinalState();

	}

	private void getXAL(AnonymClass a) {
		for(CFG c : a.getCFG()){
			XALAutomaton bck = lastAutomaton;
			XALAddState bckAut = aut;
			getXAL(c, c.getName() + "_" + a.hashCode());
			lastAutomaton = bck;
			aut = bckAut;
		}
	}

	private void getXAL(INode s) {
		if(s instanceof Node){
			getXAL((Node)s);
		} else {
			getXAL((SyncNode)s);
		}
	}

	private void getXAL(Node v) {
		XALState s = new XALState(v.getNameNoID());
		s.setNumericID(v.getID());
		aut.addState(s);
		if(v.getConstraint() != null){

		}
		if(v.isStart()){
			try {
				lastAutomaton.setInitialState(s);
			} catch (XALMalformedException e) {
				e.printStackTrace();
			}
		}
		if(v.isEnd()){
			lastAutomaton.addFinalState(s);
		}
	}

	private void getXAL(SyncNode sv) {
		XALAutomaton bck = lastAutomaton;
		XALAddState bckAut = aut;
		XALAutomaton automa = new XALAutomaton("sync_on_" + sv.getExpr() + "_" + sv.getID());
		doc.addAutomaton(automa);
		//creating metrics
		String name = automa.getId();
		String path = doc.getFilename();

		for(Node from : sv.getNodesEntered()){
			String nameMetric = "to_sync_node_" + from.getID();
			XALMetric m = new XALMetric(nameMetric, XALProduction.ProductionType.automaton , "", name, path, new XALEnumeration());

			lastAutomaton.getActionPool().addProduction(m);
			doc.getNodeFromNumericID(from.getID()).setNameMetric(nameMetric);
			doc.getNodeFromNumericID(from.getID()).setSynchronized(true);
		}


		lastAutomaton = automa;
		aut = automa;
		for(Node v : sv.getNodes()){
			getXAL(v);
		}
		for(Edge e : sv.getNewEdges()){
			getXAL(e);
		}
		lastAutomaton = bck;
		aut = bckAut;
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
		if(f == null || t == null) return;
		lastAutomaton.addTransition(new XALTransition(f,t, e.getLabel()));
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
