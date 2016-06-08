package IntermediateModel.visitors;


import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTRE;
import envirorment.BuildEnvirormentClass;
import envirorment.Env;
import heuristic.SearchTimeConstraint;

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

	public ApplyHeuristics(){
		build_base_env = new BuildEnvirormentClass();
	}
	public ApplyHeuristics(BuildEnvirormentClass env) {
		this.build_base_env = env;
	}

	public void subscribe(SearchTimeConstraint strategy){
		strategies.add(strategy);
	}

	public void analyze(ASTClass c){
		build_base_env.buildEnvClass(c);
		for(IASTMethod m : c.getMethods()){
			analyze(m, new Env(build_base_env.getEnv()));
		}
	}

	private void analyze(IASTMethod m, Env env){
		for(IASTStm stm : m.getStms()){
			if(stm instanceof ASTRE)
				analyze((ASTRE)stm, env);
			else
				analyze(stm, env);
		}
	}

	private void analyze(ASTRE r, Env env){
		//build env
		//System.err.println("Not Implemented Yet :: " + r.toString());
		env = build_base_env.checkRE(r, env);
		//call the strategies
		applyStep(r, env);
	}

	private void analyze(IASTStm r, Env env){
		//System.err.println("Not Implemented Yet :: " + r.getClass().getSimpleName());
	}

	private void applyStep(IASTStm stm, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(stm, env);
		}
	}


}
