package visitors;

import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ConvertIM;
import org.javatuples.KeyValue;
import structure.Edge;
import structure.Node;
import structure.PCFG;
import structure.SyncNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IM2PCFG extends ConvertIM {
	private List<KeyValue<String,ASTClass>> classes = new ArrayList<>();
	private PCFG pcfg;
	private Node lastNode;
	private SyncNode syncNode;
	private String lastLabel;
	private boolean isMultiLabel = false;
	private List<Node> multiLabel = new ArrayList<>();

	public IM2PCFG(List<KeyValue<String,ASTClass>> classes) {
		this.classes = classes;
	}

	public IM2PCFG() {
	}

	public void addClass(ASTClass c, String methodName){
		this.classes.add(new KeyValue<String, ASTClass>(methodName, c));
	}

	public PCFG buildPCFG(){
		//init data
		pcfg = new PCFG();

		//add all the states to the PCFG
		for(KeyValue<String,ASTClass> c : classes){
			for(IASTMethod m : c.getValue().getMethods()){
				//only work on the method specified
				if(m.getName().equals(c.getKey())) {
					addSingleClassStates(m);
				}
			}

		}
		//optimize
		pcfg.optimize();

		//check for sync blocks

		//check for call on sync methods

		return pcfg;
	}

	private void addSingleClassStates(IASTMethod c){
		lastNode = null;
		dispachStm(c.getStms());
	}


	@Override
	protected void convertForeach(ASTForEach stm) {

		Node has_next = new Node("hasNext", "", Node.TYPE.FOREACH);
		addState(has_next);

		addState(new Node(
				stm.getVar().getName() + "_takes_" + stm.getExpr().getExpressionName(),
				stm.getVar().getCode() + " : " + stm.getExpr().getCode(),
				Node.TYPE.NORMAL
		));
		this.lastLabel = "True";
		super.dispachStm(stm.getStms());
		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS);
		this.pcfg.addNode(end_for);
		Edge e = new Edge(has_next, end_for);
		e.setLabel("False");
		this.pcfg.addEdge(e);
		this.lastNode = end_for;
	}

	@Override
	protected void convertFor(ASTFor stm) {
		Node init_for = new Node("_init_for", "", Node.TYPE.USELESS);
		addState(init_for);
		if(stm.getInit().size() > 0){
			stm.getInit().forEach(this::convertRE);
		}
		Node expr_for = new Node("_expr_for", "", Node.TYPE.USELESS);
		addState(expr_for);
		if(stm.getExpr() != null){
			convertRE(stm.getExpr());
		}
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		this.lastLabel = "";
		Node post_for = new Node("_post_for", "", Node.TYPE.USELESS);
		addState(post_for);
		if(stm.getPost().size() > 0){
			stm.getPost().forEach(this::convertRE);
		}
		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS);
		this.pcfg.addNode(end_for);
		Edge e = new Edge(expr_for, end_for);
		e.setLabel("False");
		this.pcfg.addEdge(e);
		this.lastNode = end_for;
	}

	@Override
	protected void convertDoWhile(ASTDoWhile stm) {
		Node init_do_while = new Node("_init_do_while", "", Node.TYPE.USELESS);
		addState(init_do_while);
		dispachStm(stm.getStms());
		Node expr = new Node(stm.getExpr().getExpressionName(), stm.getExpr().getCode(), Node.TYPE.NORMAL);
		addState( expr );
		Edge e = new Edge( expr, init_do_while );
		e.setLabel("True");
		pcfg.addEdge(e);
		this.lastLabel = "False";
	}

	@Override
	protected void convertWhile(ASTWhile stm) {
		Node expr = new Node(stm.getExpr().getExpressionName(), stm.getExpr().getCode(), Node.TYPE.NORMAL);
		addState( expr );
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	@Override
	protected void convertSyncronized(ASTSynchronized stm) {
		syncNode = new SyncNode();
		pcfg.addNode(syncNode);
		dispachStm(stm.getStms());
		syncNode = null;
	}

	@Override
	protected void convertIf(ASTIf stm) {
		Node expr = new Node(stm.getGuard().getExpressionName(), stm.getGuard().getCode(), Node.TYPE.NORMAL);
		addState( expr );
		this.lastLabel = "True";
		super.dispachStm(stm.getIfBranch().getStms());
		this.lastLabel = "False";
		if(stm.getElseBranch() != null){
			dispachStm(stm.getElseBranch().getStms());
		}
		Node end_if = new Node("_end_if", "", Node.TYPE.USELESS);
		addState(end_if);
	}

	@Override
	protected void convertTry(ASTTry stm) {

		Node init_try = new Node("try", "", Node.TYPE.TRY);
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY);
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS);
		this.pcfg.addNode(finally_try);
		this.pcfg.addNode(end_try);
		addState(init_try);

		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.pcfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node catch_try = new Node("catch", "", Node.TYPE.USELESS);
				Edge e = new Edge(init_try, catch_try);
				this.pcfg.addNode(catch_try);
				this.pcfg.addEdge(e);
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL
				);
				Edge eInstance = new Edge(catch_try, instance);
				this.pcfg.addNode(instance);
				this.pcfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.pcfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(catch_try, end_try);
				toEnd.setLabel("False");
				this.pcfg.addEdge(toEnd);
			}
		}

		if(stm.getFinallyBranch() != null){
			this.lastNode = finally_try;
			super.dispachStm(stm.getFinallyBranch().getStms());
			Edge toEnd = new Edge(this.lastNode, end_try);
			this.pcfg.addEdge(toEnd);
		}

		this.lastNode = end_try;
	}

	@Override
	protected void convertTryResource(ASTTryResources stm) {
		Node init_try = new Node("try", "", Node.TYPE.TRY);
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY);
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS);
		this.pcfg.addNode(finally_try);
		this.pcfg.addNode(end_try);
		addState(init_try);

		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.pcfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node catch_try = new Node("catch", "", Node.TYPE.USELESS);
				Edge e = new Edge(init_try, catch_try);
				this.pcfg.addNode(catch_try);
				this.pcfg.addEdge(e);
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL
				);
				Edge eInstance = new Edge(catch_try, instance);
				this.pcfg.addNode(instance);
				this.pcfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.pcfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(catch_try, end_try);
				toEnd.setLabel("False");
				this.pcfg.addEdge(toEnd);
			}
		}

		if(stm.getFinallyBranch() != null){
			this.lastNode = finally_try;
			super.dispachStm(stm.getFinallyBranch().getStms());

			//convert the resources
			for(ASTRE r : stm.getResources()){
				Node resource = new Node(
						"close_" + r.getExpressionName(),
						r.getCode(),
						Node.TYPE.NORMAL
				);
				addState(resource);
			}

			Edge toEnd = new Edge(this.lastNode, end_try);
			this.pcfg.addEdge(toEnd);
		}

		this.lastNode = end_try;
	}

	@Override
	protected void convertASTSwitch(ASTSwitch stm) {

		Node init_switch = new Node("switch", "", Node.TYPE.SWITCH);
		Node end_switch = new Node("endSwitch", "", Node.TYPE.USELESS);

		this.pcfg.addNode(end_switch);
		addState(init_switch);

		Node check = new Node(
				"check_" + stm.getExpr().getExpressionName(),
				stm.getCode(),
				Node.TYPE.NORMAL
		);
		addState(check);

		for(ASTSwitch.ASTCase c : stm.getCases()){
			for(String label : c.getLabels()){
				Node labelSwitch = new Node(
						"equals_" + label,
						label,
						Node.TYPE.NORMAL
				);
				Edge e = new Edge(check, labelSwitch);
				this.pcfg.addNode(labelSwitch);
				this.pcfg.addEdge(e);
				this.multiLabel.add(labelSwitch);
			}
			this.isMultiLabel = true;
			super.dispachStm(c.getStms());
			Edge toEnd = new Edge(this.lastNode, end_switch);
			this.pcfg.addEdge(toEnd);
		}
	}

	@Override
	protected void convertReturn(ASTReturn stm) {
		addState(
				new Node(
						"return" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.RETURN
				)
		);
	}

	@Override
	protected void convertThrow(ASTThrow stm) {
		addState(
				new Node(
						"throw" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.THROW
				)
		);
	}

	@Override
	protected void convertContinue(ASTContinue stm) {
		addState(
				new Node("continue", stm.getCode(), Node.TYPE.CONTINUE)
		);
	}

	@Override
	protected void convertBreak(ASTBreak stm) {
		addState(
				new Node("break", stm.getCode(), Node.TYPE.BREAK)
		);
	}

	@Override
	protected void convertRE(ASTRE stm) {
		addState(
				new Node(stm.getExpressionName(), stm.getCode(), Node.TYPE.NORMAL)
		);
	}

	private void addState(Node node) {
		this.pcfg.addNode(node);
		if(this.lastNode != null && !this.isMultiLabel){
			Edge e = new Edge(this.lastNode, node);
			e.setLabel(lastLabel);
			this.pcfg.addEdge(e);
		}
		if(this.isMultiLabel){
			for(Node n : this.multiLabel){
				Edge e = new Edge(n, node);
				e.setLabel("T");
				this.pcfg.addEdge(e);
			}
			this.multiLabel.clear();
		}
		if(this.syncNode != null){
			this.syncNode.add(node);
		}
		this.lastNode = node;
		this.lastLabel = "";
	}

}
