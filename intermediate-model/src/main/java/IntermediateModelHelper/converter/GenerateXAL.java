package IntermediateModelHelper.converter;


import XAL.XALStructure.XALAddState;
import XAL.XALStructure.exception.XALMalformedException;
import XAL.XALStructure.items.*;
import intermediateModel.interfaces.IASTHasStms;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.DefaultASTVisitor;

/**
 *
 *
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class GenerateXAL {

	XALDocument xalDocument;
	XALState lastState = null;
	XALAddState lastAutomatonWhereAdd = null;
	XALAutomaton lastAutomaton = null;
	String metricValue = "";

	public XALDocument getXalDocument() {
		return xalDocument;
	}

	public void GenerateXAL(ASTClass elm) {
		xalDocument = new XALDocument(elm.getName());
	}

	public void enterASTConstructor(ASTConstructor elm) {
		XALAutomaton xalAutomaton = new XALAutomaton(elm.getName());
		for(ASTVariable v : elm.getParameters()) {
			xalAutomaton.getGlobalState().addVariable(
					new XALVariable(v.getName(), v.getType(), "", XALVariable.XALIO.I)
			);
		}
		XALState init = new XALState("init");
		xalAutomaton.addState(init);
		try {
			xalAutomaton.setInitialState(init);
		} catch (XALMalformedException e) {
			e.printStackTrace();
		}
		xalDocument.addAutomaton(xalAutomaton);
		lastState = init;
		lastAutomaton = xalAutomaton;
		lastAutomatonWhereAdd = xalAutomaton;
	}


	public void enterASTMethod(ASTMethod elm) {
		XALAutomaton xalAutomaton = new XALAutomaton(elm.getName());
		for(ASTVariable v : elm.getParameters()) {
			xalAutomaton.getGlobalState().addVariable(
					new XALVariable(v.getName(), v.getType(), "", XALVariable.XALIO.I)
			);
		}
		XALState init = new XALState("init");
		xalAutomaton.addState(init);
		try {
			xalAutomaton.setInitialState(init);
		} catch (XALMalformedException e) {
			e.printStackTrace();
		}
		lastAutomatonWhereAdd = xalAutomaton;
		if(elm.isSyncronized()){
			XALSync sync = new XALSync(elm.getName());
			xalAutomaton.addState(sync);
			lastAutomatonWhereAdd = sync;
		}
		xalDocument.addAutomaton(xalAutomaton);
		lastState = init;
		lastAutomaton = xalAutomaton;
	}



	public void enterASTBreak(ASTBreak elm) {
		XALState s = new XALState("break");
		addState(s);
	}


	public void enterASTContinue(ASTContinue elm) {
		XALState s = new XALState("continue");
		addState(s);
	}


	public void enterASTDoWhile(ASTDoWhile elm) {
		XALState _do = new XALState("do");
		XALState _enddo = new XALState("enddo");
		XALState _checkExpr = new XALState("check");
		addState(_do);
		addState(_enddo);
		addState(_checkExpr);
		lastState = _do;
		//do the stms
		for(IASTStm s : elm.getStms()){
			s.visit(this);
		}
		XALTransition texpr = new XALTransition(lastState,_do);
		lastAutomaton.addTransition(texpr);
		//expr
		this.enterASTRE(elm.getExpr());
		//return to _do
		XALTransition t = new XALTransition(lastState,_do);
		lastAutomaton.addTransition(t);
	}



	public void enterASTIf(ASTIf elm) {
		XALState _if = new XALState("if");
		XALState _end = new XALState("endif");
		lastAutomatonWhereAdd.addState(_if);
		lastAutomatonWhereAdd.addState(_end);
		XALTransition t = new XALTransition(lastState,_if);
	 	lastAutomaton.addTransition(t);
		lastState = _if;
		metricValue = XALTransition.METRIC_TRUE;
		for(IASTStm s : elm.getIfBranch().getStms()){
			s.visit(this);
		}
		XALTransition teif = new XALTransition(lastState,_end);
		lastAutomaton.addTransition(teif);
		if(elm.getElseBranch() != null){
			lastState = _if;
			metricValue = XALTransition.METRIC_FALSE;
			for(IASTStm s : elm.getElseBranch().getStms()){
				s.visit(this);
			}
		}
		XALTransition te = new XALTransition(lastState,_end);
		lastAutomaton.addTransition(te);
		lastState = _end;

	}


	public void enterASTRE(ASTRE elm) {
		XALState s = new XALState(elm.getExpressionName());
		addState(s);
	}



	public void addState(XALState state){
		lastAutomatonWhereAdd.addState(state);
		XALTransition transition = new XALTransition(this.lastState, state, this.metricValue );
		lastAutomaton.addTransition(transition);
		this.lastState = state;
		this.metricValue = "";
	}
}
