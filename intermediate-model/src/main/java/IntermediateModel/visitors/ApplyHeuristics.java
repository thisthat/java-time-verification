package IntermediateModel.visitors;


import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.*;
import IntermediateModel.structure.expression.ASTNewObject;
import XALConversion.util.Pair;
import envirorment.BuildEnvirormentClass;
import envirorment.Env;
import heuristic.SearchTimeConstraint;

import java.awt.dnd.DragGestureEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 *
 * This class is the manager that apply all the heuristics we created.
 * It goes through all {@link IASTMethod} definition of a {@link ASTClass}.
 * For each statement it extends the envirorment and apply all the subscribed strategies.
 */
public class ApplyHeuristics {

	private boolean isThread;
	public final String _THREAD_CLASS_ = "Thread";
	public final String _THREAD_INTERFACE_ = "Runnable";
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
		for(IASTMethod m : c.getMethods()){
			Env eMethod = new Env(build_base_env.getEnv());
			eMethod = build_base_env.checkPars(m.getParameters(), eMethod);
			analyze(m.getStms(), eMethod );
		}
	}

	/**
	 * The Method go through the list of {@link IASTStm} and
	 * @param stms
	 * @param env
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
			else if(stm	instanceof ASTTry){
				analyze((ASTTry)stm, env);
			}
			else if(stm	instanceof ASTTryResources){
				analyze((ASTTryResources)stm, env);
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

	private void analyze(ASTNewObject stm, Env env) {
		for(IASTRE r : stm.getParameters()){
			applyStep(r, env);
		}
	}

	private void analyze(ASTRE r, Env env){
		//build env
		build_base_env.checkRE(r, env);
		//call the strategies
		applyStep(r, env);
	}

	private void analyze(ASTFor elm, Env env) {
		Env new_env = new Env(env);
		for(ASTRE exp : elm.getInit()) {
			this.analyze(exp, new_env);
		}
		this.analyze(elm.getExpr(), new_env);
		for(ASTRE exp : elm.getPost()){
			this.analyze(exp, new_env);
		}
		this.analyze(elm.getExpr(), new_env);
		applyStep(elm, new_env);
	}

	private void analyze(ASTForEach elm, Env env) {
		Env new_env = new Env(env);
		build_base_env.setVariableInEnv(elm.getVar(), new_env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	private void analyze(ASTIf elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getGuard(), new Env(new_env));
		this.analyze(elm.getIfBranch().getStms(), new Env(new_env));
		if(elm.getElseBranch() != null)
			this.analyze(elm.getElseBranch().getStms(), new_env);
		applyStep(elm, new_env);
	}

	private void analyze(ASTReturn elm, Env env) {
		Env new_env = new Env(env);
		if(elm.getExpr() != null)
			this.analyze(elm.getExpr(), new Env(new_env));
		applyStep(elm, new_env);
	}

	private void analyze(ASTSwitch elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		for (ASTSwitch.ASTCase c : elm.getCases()) {
			this.analyze( c.getStms(), new Env(new_env));
		}
		applyStep(elm, new_env);
	}

	private void analyze(ASTSynchronized elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(),new_env);
		applyStep(elm, new_env);
	}

	private void analyze(ASTThrow elm, Env env) {
		build_base_env.checkRE(elm.getExpr(), env);
		this.analyze(elm.getExpr(), env);
		applyStep(elm, env);
	}

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

	private void analyze(ASTWhile elm, Env env) {
		Env new_env = new Env(env);
		this.analyze(elm.getExpr(), new_env);
		this.analyze(elm.getStms(), new_env);
		applyStep(elm, new_env);
	}

	private void analyze(IASTStm r, Env env){
		System.err.println("Not Implemented Yet :: " + r.getClass().getSimpleName());
	}

	private void applyStep(IASTStm stm, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(stm, env);
		}
	}

	private void applyStep(IASTRE stm, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(stm, env);
		}
	}


	public List<Pair<Integer, String>> getTimeConstraint() {
		List<Pair<Integer, String>> out = new ArrayList<>();
		for(SearchTimeConstraint s : strategies){
			out.addAll(s.getTimeConstraint());
		}
		return out;
	}
}
