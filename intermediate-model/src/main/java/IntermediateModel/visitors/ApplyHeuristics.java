package intermediateModel.visitors;


import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;
import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.heuristic.SearchTimeConstraint;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * This class is the manager that apply all the heuristics we created.
 * It goes through all {@link IASTMethod} definition of a {@link ASTClass}.
 * For each statement it extends the {@link Env} and apply all the subscribed strategies.
 */
public class ApplyHeuristics {

	private BuildEnvirormentClass build_base_env = null;
	private List<SearchTimeConstraint> strategies = new ArrayList<>();
	private List<Class<? extends SearchTimeConstraint>> strategiesTypes = new ArrayList<>();

	public ApplyHeuristics(){
		build_base_env = new BuildEnvirormentClass();
	}
	public ApplyHeuristics(BuildEnvirormentClass env) {
		this.build_base_env = env;
	}

	public void subscribe(Class<? extends SearchTimeConstraint> strategy){
		strategiesTypes.add(strategy);
	}

	public void analyze(ASTClass c){
		//instantiate the strategies
		strategies.clear();
		for(Class<? extends SearchTimeConstraint> type : strategiesTypes){
			SearchTimeConstraint a = null;
			try {
				a = type.newInstance();
				strategies.add(a);
			} catch (InstantiationException e) {
				//e.printStackTrace();
			} catch (IllegalAccessException e) {
				//e.printStackTrace();
			}
		}
		build_base_env.buildEnvClass(c);
		Env e = build_base_env.getEnv();
		//check static
		for(ASTStatic s : c.getStaticInit()){
			analyze(s.getStms(), e);
		}
		//check method
		for(IASTMethod m : c.getMethods()){
			Env eMethod = new Env(e);
			eMethod = build_base_env.checkPars(m.getParameters(), eMethod);
			analyze(m.getStms(), eMethod );
		}
	}

	/**
	 * This method is a dispatcher.
	 * It goes through the list of {@link IASTStm} and dispatch to the method that can handle that
	 * particular type of statement.
	 * <b>Particular Note:</b> from the fact that {@link ASTTryResources} extends {@link ASTTry}, the order
	 * in the if is <u><b>REALLY IMPORTANT</b></u>. The first type must be checked before otherwise it will
	 * be dispatched always to the latter type.
	 * @param stms	List of statements to analyze
	 * @param env	Environment that is visible to that list of statements
	 */
	private void analyze(List<IASTStm> stms, Env env){
		for(IASTStm stm : stms) {
			if (stm instanceof ASTRE){
				analyze((ASTRE) stm, env);
			}
			else if(stm	instanceof ASTBreak){
				continue; //nothing to check
			}
			else if(stm	instanceof ASTContinue){
				continue; //nothing to check
			}
			else if(stm	instanceof ASTDoWhile){
				analyze((ASTDoWhile)stm, env);
			}
			else if(stm	instanceof ASTFor){
				analyze((ASTFor)stm, env);
			}
			else if(stm	instanceof ASTForEach){
				analyze((ASTForEach)stm, env);
			}
			else if(stm	instanceof ASTIf){
				analyze((ASTIf)stm, env);
			}
			else if(stm	instanceof ASTReturn){
				analyze((ASTReturn)stm, env);
			}
			else if(stm	instanceof ASTSwitch){
				analyze((ASTSwitch)stm, env);
			}
			else if(stm	instanceof ASTSynchronized){
				analyze((ASTSynchronized)stm, env);
			}
			else if(stm	instanceof ASTThrow){
				analyze((ASTThrow)stm, env);
			}
			//the order is important because ASTTry is extended by ASTTryResources
			else if(stm	instanceof ASTTryResources){
				analyze((ASTTryResources)stm, env);
			}
			else if(stm	instanceof ASTTry){
				analyze((ASTTry)stm, env);
			}
			else if(stm	instanceof ASTWhile){
				analyze((ASTWhile)stm, env);
			}
			else if(stm instanceof ASTNewObject){
				analyze((ASTNewObject) stm, env);
			}
			else {
				analyze(stm, env);
			}
		}
	}

	/**
	 * ATM. there are no inner classes/method in the initialization of a new object.
	 * Therefore just check the parameters.
	 * @param stm	{@link ASTNewObject} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTNewObject stm, Env env) {
		for(IASTRE r : stm.getParameters()){
			applyStep(r, env);
		}
	}

	/**
	 * Right Expression to check. Ez.
	 * @param r		{@link ASTRE} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTRE r, Env env){
		//build env
		build_base_env.checkRE(r, env);
		//call the strategies
		applyStep(r, env);
	}

	/**
	 * In the Do-While the expression is not visible by the stms and the environment and the stms environment is not
	 * visible by the expression.
	 * @param elm	{@link ASTDoWhile} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTDoWhile elm, Env env) {
		this.analyze(elm.getStms(), new Env(env));
		this.analyze(elm.getExpr(), new Env(env));
		applyStep(elm, env);
	}

	/**
	 * The for statements can see the variables in all the three right expressions.
	 *
	 * @param elm	{@link ASTFor} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTFor elm, Env env) {
		Env new_env = new Env(env);
		for(ASTRE exp : elm.getInit()) {
			this.analyze(exp, new_env);
		}
		this.analyze(elm.getExpr(), new_env);
		for(ASTRE exp : elm.getPost()){
			this.analyze(exp, new_env);
		}
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	/**
	 * We extend the environment with the expression in the foreach and then check in the stms.
	 * @param elm	{@link ASTForEach} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTForEach elm, Env env) {
		Env new_env = new Env(env);
		build_base_env.setVariableInEnv(elm.getVar(), new_env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	/**
	 * The if share the guard visibility with both <b>then</b> and <b>else</b> branches.
	 * But, the <b>then</b> and <b>else</b> have a different environment.
	 * @param elm	{@link ASTIf} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTIf elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getGuard(), new_env);
		this.analyze(elm.getIfBranch().getStms(), new Env(new_env));
		if(elm.getElseBranch() != null)
			this.analyze(elm.getElseBranch().getStms(), new Env(new_env));
		applyStep(elm, new_env);
	}

	/**
	 * The return extends the environment with the expression (if it is present).
	 * @param elm	{@link ASTReturn} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTReturn elm, Env env) {
		if(elm.getExpr() != null)
			this.analyze(elm.getExpr(), env);
		applyStep(elm, env);
	}

	/**
	 * The switch shares the visibility of the expression with each case block.
	 * However, each case block has its own environment.
	 * @param elm	{@link ASTSwitch} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTSwitch elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		for (ASTSwitch.ASTCase c : elm.getCases()) {
			this.analyze( c.getStms(), new Env(new_env));
		}
		applyStep(elm, new_env);
	}

	/**
	 * In a synchronized block the statements can see the expression on which we lock on the object.
	 * @param elm	{@link ASTSynchronized} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTSynchronized elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	/**
	 * Throw statement extends the environment with the right expression that it carries.
	 * @param elm	{@link ASTThrow} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTThrow elm, Env env) {
		build_base_env.checkRE(elm.getExpr(), env);
		this.analyze(elm.getExpr(), env);
		applyStep(elm, env);
	}

	/**
	 * The try/catch/finally branches have a separate environment.
	 * Moreover, each catch block has its own environment.
	 * @param elm	{@link ASTTry} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTTry elm, Env env) {
		Env new_env_try = new Env(env);
		Env new_env_finally = new Env(env);

		analyze(elm.getTryBranch().getStms(), new_env_try);
		for(ASTTry.ASTCatchBranch catchBranch : elm.getCatchBranch()){
			Env new_env_catch = new Env(env);
			analyze( catchBranch.getStms(), new_env_catch );
		}
		if(elm.getFinallyBranch() != null)
			analyze(elm.getFinallyBranch().getStms(), new_env_finally);
		applyStep(elm, env);
	}

	/**
	 * The try/catch/finally branches have a separate environment.
	 * Moreover, each catch block has its own environment.
	 * All the blocks share the environment with the resources.
	 * @param elm	{@link ASTTryResources} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTTryResources elm, Env env) {
		Env env_resource = new Env(env);
		for(ASTRE r : elm.getResources()){
			this.analyze(r, env_resource);
		}
		Env new_env_try = new Env(env_resource);
		Env new_env_finally = new Env(env_resource);
		analyze(elm.getTryBranch().getStms(), new_env_try);
		for(ASTTry.ASTCatchBranch catchBranch : elm.getCatchBranch()){
			Env new_env_catch = new Env(env_resource);
			analyze( catchBranch.getStms(), new_env_catch );
		}
		if(elm.getFinallyBranch() != null)
			analyze(elm.getFinallyBranch().getStms(), new_env_finally);
		applyStep(elm, env_resource);
	}

	/**
	 * The statement in the while can access to the guard.
	 * @param elm	{@link ASTWhile} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(ASTWhile elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	/**
	 * Fall over method. If we forgot something, we will know ;)
	 * @param r	{@link IASTStm} instruction to check.
	 * @param env	{@link Env} visible by the instruction.
	 */
	private void analyze(IASTStm r, Env env){
		System.err.println("Not Implemented Yet :: " + r.getClass().getSimpleName());
	}

	private void applyStep(IASTStm stm, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(stm, env);
		}
	}

	private void applyStep(IASTRE re, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(re, env);
		}
	}

	public List<Triplet<Integer, String, Class>> getTimeConstraint() {
		List<Triplet<Integer, String, Class>> out = new ArrayList<>();
		for(SearchTimeConstraint s : strategies){
			out.addAll(s.getTimeConstraint());
		}
		return out;
	}
}
