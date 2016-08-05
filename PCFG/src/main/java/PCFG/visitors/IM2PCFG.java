package PCFG.visitors;

import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexParameter;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.ConvertIM;
import org.javatuples.KeyValue;
import PCFG.structure.*;
import PCFG.visitors.helper.GenerateMethodSyncCallList;
import PCFG.visitors.helper.SyncMethodCall;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * The following class convert the IM of a set of methods in a CFG first.
 * Then, it create cross links between the CFGs of the methods where there is:
 * <ul>
 *     <li>a synchronization on the same variables</li>
 *     <li>a method call on the same synchronized methods</li>
 * </ul>
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IM2PCFG extends ConvertIM {

	private List<KeyValue<String,ASTClass>> classes = new ArrayList<>();
	private PCFG pcfg;
	private CFG  lastCfg;
	private Node lastNode;
	private SyncNode syncNode;
	private String lastLabel = "";
	private boolean isMultiLabel = false;
	private List<Node> multiLabel = new ArrayList<>();
	private IndexingFile classIndexer = new IndexingFile();
	private List<IndexData> indexs = new ArrayList<>();
	private String lastClass = "";
	private List<Triplet<String, IASTStm, Class>> constraints = new ArrayList<>();

	public IM2PCFG(List<KeyValue<String,ASTClass>> classes) {
		this.classes = classes;
	}

	public IM2PCFG() {

	}

	/**
	 * Insert a class in the list of classes to process for creating the PCFG.
	 * Moreover, the method will index the class and extract the time constraint in it.
	 * @param c				Class to analyze
	 * @param methodName	Method of the class to analyze
	 */
	public void addClass(ASTClass c, String methodName){

		List<Triplet<String,IASTStm,Class>> currentClassConstraint = ApplyHeuristics.getConstraint(c);
		IASTMethod met = null;
		for(IASTMethod m : c.getMethods()){
			if(m.getName().equals(methodName)){
				met = m;
			}
		}
		if(met != null){
			int startLine =  ((IASTStm) met).getLine();
			int endLine   =  ((IASTStm) met).getLineEnd();
			for(Triplet<String,IASTStm,Class> time : currentClassConstraint){
				int line = time.getValue1().getLine();
				if(startLine <= line && line <= endLine){
					this.constraints.add(time);
				}
			}
		}

		this.classes.add(new KeyValue<String, ASTClass>(methodName, c));
		IndexData index = classIndexer.index(c);
		indexs.add( index );
	}

	public PCFG buildPCFG(){
		//init data
		pcfg = new PCFG();

		//add all the states to the PCFG
		for(KeyValue<String,ASTClass> c : classes){
			for(IASTMethod m : c.getValue().getMethods()){
				//only work on the method specified
				if(m.getName().equals(c.getKey())) {
					addSingleClassStates(c.getValue(), m);
				}
			}
		}

		//optimize
		pcfg.optimize();

		//check for sync blocks
		calculateSyncBlock();

		//check for call on sync methods
		calculateSyncCall();

		return pcfg;
	}

	private void calculateSyncCall() {
		HashMap<ASTClass,List<SyncMethodCall>> syncCalls = new HashMap<>();

		for(KeyValue<String,ASTClass> c : classes){
			for(IASTMethod m : c.getValue().getMethods()){
				//only work on the method specified
				if(m.getName().equals(c.getKey())) {
					ASTClass _class = c.getValue();
					GenerateMethodSyncCallList helper = new GenerateMethodSyncCallList(_class, m);
					syncCalls.put(_class, helper.calculateSyncCallList());
				}
			}
		}

		//we have the list of all
		for(KeyValue<String,ASTClass> cOut : classes){
			ASTClass outClass = cOut.getValue();
			for(KeyValue<String,ASTClass> cIn : classes){
				if(cIn.equals(cOut)) continue;
				ASTClass inClass = cIn.getValue();
				List<SyncMethodCall> outter = syncCalls.get(outClass);
				List<SyncMethodCall> inner  = syncCalls.get(inClass);
				for(SyncMethodCall outMethod : outter){
					for(SyncMethodCall inMethod : inner){
						if(outMethod.equals(inMethod)){
							createLink(outMethod, inMethod);
						}
					}
				}
			}
		}



	}

	private void createLink(SyncMethodCall outMethod, SyncMethodCall inMethod) {
		Node from = null;
		Node to = null;
		String codeOut = outMethod.getNode().getCode();
		String codeIn  = inMethod.getNode().getCode();
		for(Node v : this.pcfg.getV()){
			String code = v.getCode();
			if(code.equals(codeOut)){
				from = v;
			} else if(code.equals(codeIn)){
				to = v;
			}
		}
		try {
			SyncEdge e = new SyncEdge(from, to);
			this.pcfg.addSyncEdge(e);
		} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
			malformedSyncEdge.printStackTrace();
		}
	}

	private void calculateSyncBlock() {
		//get the sync blocks of the methods that we are looking for
		List<IndexSyncBlock> syncBlocks = new ArrayList<>();
		for(KeyValue<String,ASTClass> c : classes){
			for(IndexData data : indexs){
				for(IndexSyncBlock s : data.getListOfSyncBlocks()){
					if(
							s.getClassName().equals(
									c.getValue().getName()
							)
							&&
							s.getMethodName().equals(
									c.getKey())
							)
					{
						syncBlocks.add(s);
					}
				}
			}
		}
		//everyone is checked against everyone
		for(IndexSyncBlock outter : syncBlocks){
			IndexParameter varOutter = outter.getEnv().getVar(outter.getExpr());
			if(varOutter == null) { //could be a method call or something else than a simple var :(
				//for the moment we consider only variables
				continue;
			}
			for(IndexSyncBlock inner : syncBlocks) {
				if (outter == inner) { //we do not check myself with myself
					continue;
				}
				IndexParameter varInner = inner.getEnv().getVar(inner.getExpr());
				if(varInner == null){//could be a method call or something else than a simple var :(
					//for the moment we consider only variables
					continue;
				}
				if(varInner.getType().equals(varOutter.getType())){
					//we gotta a match
					SyncNode outSync = pcfg.getSyncNodeByExpr( outter.getExpr(), outter.getLine(), outter.getClassName() );
					SyncNode inSync  = pcfg.getSyncNodeByExpr( inner.getExpr(),  inner.getLine(),  inner.getClassName()  );
					SyncEdge sEdge = null;
					try {
						sEdge = new SyncEdge(outSync,inSync);
						pcfg.addSyncEdge(sEdge);
					} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
						malformedSyncEdge.printStackTrace();
					}
				}
			}
		}
	}

	private void addSingleClassStates(ASTClass c, IASTMethod m){
		lastNode = null;
		lastCfg = new CFG(c.getName());
		this.pcfg.addCFG(lastCfg);
		lastClass = c.getName();
		dispachStm(m.getStms());
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

		//back to the expr
		Edge eBack = new Edge(this.lastNode, has_next);
		this.lastCfg.addEdge(eBack);

		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS);
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(has_next, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
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
		//to expr again
		Edge eBack = new Edge(this.lastNode, expr_for);
		this.lastCfg.addEdge(eBack);
		//close the for
		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS);
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(expr_for, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
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
		this.lastCfg.addEdge(e);
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	@Override
	protected void convertWhile(ASTWhile stm) {
		Node expr = new Node(stm.getExpr().getExpressionName(), stm.getExpr().getCode(), Node.TYPE.NORMAL);
		addState( expr );
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		//back to the expr
		Edge e = new Edge( this.lastNode , expr );
		this.lastCfg.addEdge(e);
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	@Override
	protected void convertSyncronized(ASTSynchronized stm) {
		syncNode = new SyncNode(stm.getExpr().getCode(), stm.getLine(), this.lastClass );
		this.lastCfg.addNode(syncNode);
		dispachStm(stm.getStms());
		syncNode = null;
	}

	@Override
	protected void convertIf(ASTIf stm) {
		Node expr = new Node(stm.getGuard().getExpressionName(), stm.getGuard().getCode(), Node.TYPE.NORMAL);
		Node end_if = new Node("_end_if", "", Node.TYPE.USELESS);
		addState( expr );
		this.lastLabel = "True";
		super.dispachStm(stm.getIfBranch().getStms());
		//jump to the end
		Edge eEnd = new Edge(this.lastNode, end_if);
		this.lastCfg.addEdge(eEnd);

		this.lastLabel = "False";
		this.lastNode = expr;
		if(stm.getElseBranch() != null){
			dispachStm(stm.getElseBranch().getStms());
		}
		addState(end_if);
	}

	@Override
	protected void convertTry(ASTTry stm) {

		Node init_try = new Node("try", "", Node.TYPE.TRY);
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY);
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS);
		this.lastCfg.addNode(finally_try);
		this.lastCfg.addNode(end_try);
		addState(init_try);


		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS);
			Edge e = new Edge(init_try, catch_try);
			this.lastCfg.addNode(catch_try);
			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL
				);
				Edge eInstance = new Edge(catch_try, instance);
				this.lastCfg.addNode(instance);
				this.lastCfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.lastCfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(instance, finally_try);
				toEnd.setLabel("False");
				this.lastCfg.addEdge(toEnd);
			}
		}

		if(stm.getFinallyBranch() != null){
			this.lastNode = finally_try;
			super.dispachStm(stm.getFinallyBranch().getStms());
			Edge toEnd = new Edge(this.lastNode, end_try);
			this.lastCfg.addEdge(toEnd);
		} else {
			Edge finalEdge = new Edge(finally_try, end_try);
			this.lastCfg.addEdge(finalEdge);
		}

		this.lastNode = end_try;
	}

	@Override
	protected void convertTryResource(ASTTryResources stm) {
		Node init_try = new Node("try", "", Node.TYPE.TRY);
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY);
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS);
		this.lastCfg.addNode(finally_try);
		this.lastCfg.addNode(end_try);
		addState(init_try);

		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS);
			Edge e = new Edge(init_try, catch_try);
			this.lastCfg.addNode(catch_try);
			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL
				);
				Edge eInstance = new Edge(catch_try, instance);
				this.lastCfg.addNode(instance);
				this.lastCfg.addEdge(eInstance);
				this.lastNode = instance;
				this.lastLabel = "True";
				super.dispachStm(c.getStms());
				//to finally
				Edge toFinallyFromCatch = new Edge(this.lastNode, finally_try);
				this.lastCfg.addEdge(toFinallyFromCatch);
				//from catch to end try
				Edge toEnd = new Edge(instance, finally_try);
				toEnd.setLabel("False");
				this.lastCfg.addEdge(toEnd);
			}
		}

		this.lastNode = finally_try;
		if(stm.getFinallyBranch() != null){
			super.dispachStm(stm.getFinallyBranch().getStms());
		}
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
		this.lastCfg.addEdge(toEnd);

		this.lastNode = end_try;
	}

	@Override
	protected void convertASTSwitch(ASTSwitch stm) {

		Node init_switch = new Node("switch", "", Node.TYPE.SWITCH);
		Node end_switch = new Node("endSwitch", "", Node.TYPE.USELESS);

		this.lastCfg.addNode(end_switch);
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
				this.lastCfg.addNode(labelSwitch);
				this.lastCfg.addEdge(e);
				this.multiLabel.add(labelSwitch);
			}
			this.isMultiLabel = true;
			super.dispachStm(c.getStms());
			Edge toEnd = new Edge(this.lastNode, end_switch);
			this.lastCfg.addEdge(toEnd);
		}
		this.lastLabel = "";
		this.lastNode = end_switch;
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
		Triplet<String, IASTStm, Class> c = getConstraint(stm);
		if(c != null){
			this.lastLabel = this.lastLabel + "[" + c.getValue0() + "]";
		}
		addState(
				new Node(stm.getExpressionName(), stm.getCode(), Node.TYPE.NORMAL)
		);
	}

	private void addState(Node node) {
		this.lastCfg.addNode(node);
		if(this.lastNode != null && !this.isMultiLabel){
			Edge e = new Edge(this.lastNode, node);
			e.setLabel(lastLabel);
			this.lastCfg.addEdge(e);
		}
		if(this.isMultiLabel){
			for(Node n : this.multiLabel){
				Edge e = new Edge(n, node);
				e.setLabel("True");
				this.lastCfg.addEdge(e);
			}
			this.isMultiLabel = false;
			this.multiLabel.clear();
		}
		if(this.syncNode != null){
			this.syncNode.add(node);
		}
		this.lastNode = node;
		this.lastLabel = "";
	}

	private Triplet<String, IASTStm, Class> getConstraint(ASTRE r){
		for(Triplet<String, IASTStm, Class> c : this.constraints){
			if(c.getValue1().equals(r)){
				return c;
			}
		}
		return null;
	}
}
