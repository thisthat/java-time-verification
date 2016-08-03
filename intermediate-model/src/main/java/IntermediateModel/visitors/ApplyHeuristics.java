package intermediateModel.visitors;


import IntermediateModelHelper.CheckExpression;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTRE;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.*;
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
public class ApplyHeuristics extends ParseIM {

	protected List<SearchTimeConstraint> strategies = new ArrayList<>();
	protected List<Class<? extends SearchTimeConstraint>> strategiesTypes = new ArrayList<>();

	public ApplyHeuristics(){
		build_base_env = BuildEnvirormentClass.getInstance();
	}
	public ApplyHeuristics(BuildEnvirormentClass env) {
		this.build_base_env = env;
	}

	public void subscribe(Class<? extends SearchTimeConstraint> strategy){
		strategiesTypes.add(strategy);
	}

	/**
	 * This method trigger the application of the heuristic and analysis of the code.
	 * @param c Class to analyze.
	 */
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
				e.printStackTrace();
			}
		}
		Env base = super.createBaseEnv(c);
		//check method
		for(IASTMethod m : c.getMethods()){
			Env eMethod = new Env(base);
			eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
			analyze(m.getStms(), eMethod );
		}
	}


	@Override
	protected void analyzeEveryStm(IASTStm elm, Env env) {
		applyStep(elm, env);
	}

	private void applyStep(IASTStm stm, Env env){
		for(SearchTimeConstraint s : strategies){
			s.next(stm, env);
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
