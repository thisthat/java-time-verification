package PCFG.converter.xal;

import IntermediateModel.structure.ASTAttribute;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTRE;
import PCFG.converter.IConverter;
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
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;
/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class ToXAL implements IConverter {


	XALDocument doc = null;
	XALAddState aut = null;
	XALAutomaton lastAutomaton = null;
	XALGlobalState globalVars = new XALGlobalState();

	List<ASTAttribute> attribute;
	ASTClass c;
	List<Triplet<String,String, ASTRE>> calls;

	String fileName = "";
	String lastClassName = "";

	public ToXAL(ASTClass c) {
		this.c = c;
		this.attribute = c.getAttributes();
		FindMethodCall find = new FindMethodCall(this.c);
		calls = find.getMethodCalls();
	}

	@Override
	public String convert(PCFG pcfg) {
		GeneratePHP php = new GeneratePHP(pcfg, c);
		this.fileName = php.getFileName();
		php.generateClass();
		String name = "";
		for(CFG c : pcfg.getCFG()){
			name +=  c.getName() + "_";
		}
		return getXAL(pcfg,name).toString();
	}

	public XALDocument getXAL(PCFG pcfg, String filename) {
		pcfg = pcfg.optimize( new OptimizeForXAL() );
		XALDocument document = new XALDocument(filename);

		List<Pair<String,String>> vars = (List<Pair<String,String>>) pcfg.getAnnotation().get(String.valueOf(PCFG.DefaultAnnotation.GlobalVars));
		for(Pair<String,String> v : vars){
			XALVariable var = new XALVariable(v.getValue1(),v.getValue0(),"");
			globalVars.addVariable(var);
		}
		this.doc = document;
		for(CFG c : pcfg.getCFG()){
			lastClassName =  GeneratePHP.sanitize(c.getName() + c.getID());
			getXAL(c);
		}
		/*for(SyncEdge sEdge : pcfg.getESync()){

		}*/
		//add method call

		return document;
	}


	private void getXAL(CFG cfg){
		getXAL(cfg, cfg.getName());
	}

	private void getXAL(CFG cfg, String name) {
		XALAutomaton automa = new XALAutomaton(name);
		automa.setGlobalState(globalVars);
		doc.addAutomaton(automa);
		lastAutomaton = automa;
		aut = automa;
		for(Node v : cfg.getV()){
			getXAL(v);
		}
		if(aut.getStates().size() == 0){
			System.err.println("[DEBUG] No state detected for " + name + ". Switch on empty strategy");
			XALState s = new XALState("empty");
			s.setNumericID(0);
			aut.addState(s);
			lastAutomaton.addFinalState(s);
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
		//add vars.
		/*for (ASTAttribute a : attribute) {
			lastAutomaton.getGlobalState().addVariable(
					new XALVariable(a.getName(), a.getType(), /*a.getExpr() != null ? a.getExpr().getCode() :  "", XALVariable.XALIO.I)
			);
		}*/
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
		//method call?
		for(Triplet<String,String, ASTRE> call : calls){
			if(v.equals(call.getValue2())){
				String name = call.getValue0() + "::" + call.getValue1();
				String path = call.getValue0().replace(".","_") + ".xal";
				String nameMetric = "method_call_to_" + call.getValue2().hashCode();
				XALMetric m = new XALMetric(nameMetric, XALProduction.ProductionType.automaton , "", name, path, new XALEnumeration());
				lastAutomaton.getActionPool().addProduction(m);
				s.setNameMetric(nameMetric);
			}
		}
		//create action to PHP code
		String nameAction = "code" + GeneratePHP.sanitize(v.getName());
		String className = lastClassName;
		String methodName = GeneratePHP.sanitize(v.getName());
		XALAction action = new XALAction(nameAction, XALProduction.ProductionType.object, className, methodName, fileName);
		lastAutomaton.getActionPool().addProduction(action);
		s.setNameAction(nameAction);
	}

	private void getXAL(SyncNode sv) {
		XALAutomaton bck = lastAutomaton;
		XALAddState bckAut = aut;
		XALAutomaton automa = new XALAutomaton("sync_on_" + sv.getExpr() + "_" + sv.getID());
		automa.setGlobalState(globalVars);
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
