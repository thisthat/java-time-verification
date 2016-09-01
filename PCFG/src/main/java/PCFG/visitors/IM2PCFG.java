package PCFG.visitors;

import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexParameter;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import PCFG.structure.*;
import PCFG.structure.anonym.AnonymClass;
import PCFG.structure.edge.AnonymEdge;
import PCFG.structure.edge.Edge;
import PCFG.structure.edge.SyncEdge;
import PCFG.structure.node.Node;
import PCFG.structure.node.SyncNode;
import PCFG.visitors.helper.GenerateMethodSyncCallList;
import PCFG.visitors.helper.SyncMethodCall;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.ConvertIM;
import intermediateModel.visitors.DefaultASTVisitor;
import org.javatuples.KeyValue;
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
	private IHasCFG lastPCFG = null;
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

	public int getConstraintsSize() {
		return constraints.size();
	}

	/**
	 * Insert a class in the list of classes to process for creating the PCFG.
	 * Moreover, the method will index the class and extract the time constraint in it.
	 * @param c				Class to analyze
	 * @param methodName	Method of the class to analyze
	 */
	public void addClass(ASTClass c, String methodName){
		addClass(c, methodName, true);
	}
	/**
	 * Insert a class in the list of classes to process for creating the PCFG.
	 * Moreover, the method will index the class and extract the time constraint in it.
	 * @param c				Class to analyze
	 * @param methodName	Method of the class to analyze
	 * @param reindex		If true override the value in the DB
	 */
	public void addClass(ASTClass c, String methodName, boolean reindex){

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
		IndexData index = classIndexer.index(c, reindex);
		indexs.add( index );
	}

	public PCFG buildPCFG(){
		//init data
		pcfg = new PCFG();
		lastPCFG = pcfg;
		//add all the states to the PCFG
		for(KeyValue<String,ASTClass> c : classes){
			//consider also hidden methods
			c.getValue().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTMethod(ASTMethod m) {
					if(m.getName().equals(c.getKey())) {
						addSingleClassStates(c.getValue(), m);
					}
				}

				@Override
				public void enterASTConstructor(ASTConstructor m) {
					if(m.getName().equals(c.getKey())) {
						addSingleClassStates(c.getValue(), m);
					}
				}
			});
		}

		//optimize
		pcfg.optimize();

		//check for sync blocks
		//calculateSyncBlock();

		//check for call on sync methods
		calculateSyncCall();

		return pcfg;
	}

	private void calculateSyncCall() {
		HashMap<KeyValue<String,ASTClass>,List<SyncMethodCall>> syncCalls = new HashMap<>();

		for(KeyValue<String,ASTClass> c : classes){
			//consider also hidden methods
			c.getValue().visit(new DefaultASTVisitor(){
				@Override
				public void enterASTMethod(ASTMethod elm) {
					if(elm.getName().equals(c.getKey())) {
						ASTClass _class = c.getValue();
						GenerateMethodSyncCallList helper = new GenerateMethodSyncCallList(_class, elm);
						syncCalls.put(c, helper.calculateSyncCallList());
					}
				}

				@Override
				public void enterASTConstructor(ASTConstructor elm) {
					if(elm.getName().equals(c.getKey())) {
						ASTClass _class = c.getValue();
						GenerateMethodSyncCallList helper = new GenerateMethodSyncCallList(_class, elm);
						syncCalls.put(c, helper.calculateSyncCallList());
					}
				}
			});
		}

		//we have the list of all -> create the link between 'em
		for(KeyValue<String,ASTClass> cOut : classes){
			for(KeyValue<String,ASTClass> cIn : classes){
				if(cIn.equals(cOut)) continue;
				List<SyncMethodCall> outter = syncCalls.get(cOut);
				List<SyncMethodCall> inner  = syncCalls.get(cIn);
				for(SyncMethodCall outMethod : outter){
					for(SyncMethodCall inMethod : inner){
						if(outMethod.equalsBySignature(inMethod)){
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
		ASTRE codeOut = outMethod.getNode();
		ASTRE codeIn  = inMethod.getNode();
		for(Node v : this.pcfg.getV()){
			if(v.equals(codeOut)){
				from = v;
			} else if(v.equals(codeIn)){
				to = v;
			}
		}
		try {
			SyncEdge e = new SyncEdge(from, to);
			this.pcfg.addSyncEdge(e);
		} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
			malformedSyncEdge.printStackTrace();
		} catch (Exception e) {
			System.out.println("error parsing: ");
			for(KeyValue<String,ASTClass> c : classes){
				System.out.println("\t" + c.getValue().toString() + " :: " + c.getKey());
			}
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
					if(outSync == null || inSync == null){
						// we have smt in the hidden class -> How to handle?
						//for the moment we say there is a link between the two nodes that handle the sync call
						if(outSync == null){
							pcfg.getNodeInBound( outter.getStart(), outter.getEnd() );
						}
						if(inSync == null){

						}
					} else {
						//link between two different sync blocks
						try {
							sEdge = new SyncEdge(outSync, inSync);
							pcfg.addSyncEdge(sEdge);
						} catch (SyncEdge.MalformedSyncEdge malformedSyncEdge) {
							malformedSyncEdge.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void addSingleClassStates(ASTClass c, IASTMethod m){
		lastNode = null;
		lastCfg = new CFG(c.getName() + "::" + m.getName());
		lastPCFG.addCFG(lastCfg);
		lastClass = c.getName();
		dispachStm(m.getStms());
	}

	private void addSingleClassStates(ASTHiddenClass c, IASTMethod m){
		Node bckNode = lastNode;
		CFG bck = lastCfg;
		String bckLastClass = lastClass;
		IHasCFG bckLastPCFG = lastPCFG;
		//new anonym
		AnonymClass anon = new AnonymClass();
		lastCfg.addNode(anon);
		AnonymEdge anonEdge = new AnonymEdge(lastNode,anon);
		lastCfg.addEdge(anonEdge);
		//init
		lastNode = null;
		lastCfg = new CFG("anonymous" + "::" + m.getName());
		lastPCFG = anon;
		lastPCFG.addCFG(lastCfg);
		lastClass = "anonymous";
		dispachStm(m.getStms());
		lastCfg = bck;
		lastNode = bckNode;
		lastClass = bckLastClass;
		lastPCFG = bckLastPCFG;
	}


	@Override
	protected void convertForeach(ASTForEach stm) {

		Node has_next = new Node("hasNext", "", Node.TYPE.FOREACH, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(has_next);
		ASTVariable var = stm.getVar();
		addState(new Node(
				var.getName() + "_takes_" + stm.getExpr().getExpressionName(),
				var.getCode() + " : " + stm.getExpr().getCode(),
				Node.TYPE.NORMAL,
				var.getStart(), var.getEnd(), var.getLine()
		));
		this.lastLabel = "True";
		super.dispachStm(stm.getStms());

		//back to the expr
		Edge eBack = new Edge(this.lastNode, has_next);
		this.lastCfg.addEdge(eBack);

		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(has_next, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
		this.lastNode = end_for;
	}

	@Override
	protected void convertFor(ASTFor stm) {
		Node init_for = new Node("_init_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_for);
		if(stm.getInit().size() > 0){
			stm.getInit().forEach(this::convertRE);
		}
		Node expr_for = new Node("_expr_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(expr_for);
		if(stm.getExpr() != null){
			convertRE(stm.getExpr());
		}
		this.lastLabel = "True";
		dispachStm(stm.getStms());
		this.lastLabel = "";
		Node post_for = new Node("_post_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(post_for);
		if(stm.getPost().size() > 0){
			stm.getPost().forEach(this::convertRE);
		}
		//to expr again
		Edge eBack = new Edge(this.lastNode, expr_for);
		this.lastCfg.addEdge(eBack);
		//close the for
		Node end_for = new Node("_end_for", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(end_for);
		Edge e = new Edge(expr_for, end_for);
		e.setLabel("False");
		this.lastCfg.addEdge(e);
		this.lastNode = end_for;
	}

	@Override
	protected void convertDoWhile(ASTDoWhile stm) {
		Node init_do_while = new Node("_init_do_while", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		addState(init_do_while);
		dispachStm(stm.getStms());
		ASTRE ex = stm.getExpr();
		Node expr = new Node(ex.getExpressionName(), ex.getCode(), Node.TYPE.NORMAL, ex.getStart(), ex.getEnd(), ex.getLine() );
		addState( expr );
		Edge e = new Edge( expr, init_do_while );
		e.setLabel("True");
		this.lastCfg.addEdge(e);
		this.lastLabel = "False";
		this.lastNode = expr;
	}

	@Override
	protected void convertWhile(ASTWhile stm) {
		Node expr = new Node(stm.getExpr().getExpressionName(), stm.getExpr().getCode(), Node.TYPE.NORMAL, stm.getStart(), stm.getEnd(), stm.getLine());
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
	protected void convertSynchronized(ASTSynchronized stm) {
		syncNode = new SyncNode(stm.getExpr().getCode(), stm.getStart(), stm.getEnd(), stm.getLine(), this.lastClass );
		this.lastCfg.addNode(syncNode);
		dispachStm(stm.getStms());
		syncNode = null;
	}

	@Override
	protected void convertIf(ASTIf stm) {
		ASTRE ex = stm.getGuard();
		Node expr = new Node(ex.getExpressionName(), ex.getCode(), Node.TYPE.NORMAL, ex.getStart(), ex.getEnd(), ex.getLine() );
		Node end_if = new Node("_end_if", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
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

		Node init_try = new Node("try", "", Node.TYPE.TRY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(finally_try);
		this.lastCfg.addNode(end_try);
		addState(init_try);


		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
			Edge e = new Edge(init_try, catch_try);
			this.lastCfg.addNode(catch_try);
			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
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
		Node init_try = new Node("try", "", Node.TYPE.TRY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node finally_try = new Node("finally", "", Node.TYPE.FINALLY, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_try = new Node("endtry", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
		this.lastCfg.addNode(finally_try);
		this.lastCfg.addNode(end_try);
		addState(init_try);

		super.dispachStm(stm.getTryBranch().getStms());

		//go to finally
		Edge toFinally = new Edge(this.lastNode, finally_try);
		this.lastCfg.addEdge(toFinally);

		if(stm.getCatchBranch().size() > 0){
			Node catch_try = new Node("catch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());
			Edge e = new Edge(init_try, catch_try);
			this.lastCfg.addNode(catch_try);
			this.lastCfg.addEdge(e);
			for(ASTTry.ASTCatchBranch c : stm.getCatchBranch()){
				Node instance = new Node(
						c.getExpr().getName() + "_instanceOf_" + c.getExpr().getType(),
						c.getCode(),
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
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
					Node.TYPE.NORMAL,
					r.getStart(), r.getEnd(), r.getLine()
			);
			addState(resource);
		}

		Edge toEnd = new Edge(this.lastNode, end_try);
		this.lastCfg.addEdge(toEnd);

		this.lastNode = end_try;
	}

	@Override
	protected void convertASTSwitch(ASTSwitch stm) {

		Node init_switch = new Node("switch", "", Node.TYPE.SWITCH, stm.getStart(), stm.getEnd(), stm.getLine());
		Node end_switch = new Node("endSwitch", "", Node.TYPE.USELESS, stm.getStart(), stm.getEnd(), stm.getLine());

		this.lastCfg.addNode(end_switch);
		addState(init_switch);

		Node check = new Node(
				"check_" + stm.getExpr().getExpressionName(),
				stm.getExpr().getCode(),
				Node.TYPE.NORMAL,
				stm.getExpr().getStart(), stm.getExpr().getEnd(), stm.getExpr().getLine()
		);
		addState(check);

		for(ASTSwitch.ASTCase c : stm.getCases()){
			for(String label : c.getLabels()){
				Node labelSwitch = new Node(
						"equals_" + label,
						label,
						Node.TYPE.NORMAL,
						c.getStart(), c.getEnd(), c.getLine()
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
		int start, end, line;
		start = stm.getExpr() != null ? stm.getExpr().getStart() : stm.getStart();
		end   = stm.getExpr() != null ? stm.getExpr().getEnd()   : stm.getEnd();
		line  = stm.getExpr() != null ? stm.getExpr().getLine()  : stm.getLine();
		addState(
				new Node(
						"return" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.RETURN,
						start, end, line
				)
		);
	}

	@Override
	protected void convertThrow(ASTThrow stm) {
		int start, end, line;
		start = stm.getExpr() != null ? stm.getExpr().getStart() : stm.getStart();
		end   = stm.getExpr() != null ? stm.getExpr().getEnd()   : stm.getEnd();
		line  = stm.getExpr() != null ? stm.getExpr().getLine()  : stm.getLine();
		addState(
				new Node(
						"throw" + ( stm.getExpr() != null ? "_" + stm.getExpr().getExpressionName() : ""),
						stm.getCode(),
						Node.TYPE.THROW,
						start, end, line
				)
		);
	}

	@Override
	protected void convertContinue(ASTContinue stm) {

		addState(
				new Node("continue", stm.getCode(), Node.TYPE.CONTINUE, stm.getStart(), stm.getEnd(), stm.getLine())
		);
	}

	@Override
	protected void convertBreak(ASTBreak stm) {
		addState(
				new Node("break", stm.getCode(), Node.TYPE.BREAK, stm.getStart(), stm.getEnd(), stm.getLine())
		);
	}

	@Override
	protected void convertRE(ASTRE r) {
		Triplet<String, IASTStm, Class> c = getConstraint(r);
		if(c != null){
			this.lastLabel = this.lastLabel + "[" + c.getValue0() + "]";
		}
		addState(
				new Node(r.getExpressionName(), r.getCode(), Node.TYPE.NORMAL, r.getStart(), r.getEnd(), r.getLine())
		);
		if(r != null && r.getExpression() != null) {
			r.getExpression().visit(new DefaultASTVisitor() {
				@Override
				public void enterASTNewObject(ASTNewObject elm) {
					convertASTNewObject(elm);
				}
			});
		}
	}

	List<ASTHiddenClass> visitedHidden = new ArrayList<>();
	@Override
	protected void convertASTNewObject(ASTNewObject stm) {
		ASTHiddenClass hc = stm.getHiddenClass();
		if(hc != null && !visitedHidden.contains(hc)){
			visitedHidden.add(hc);
			this.convertASTHiddenClass(hc);
		}
	}

	@Override
	protected void convertASTHiddenClass(ASTHiddenClass stm) {
		for(IASTMethod m : stm.getMethods()){
			addSingleClassStates(stm, m);
		}
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
