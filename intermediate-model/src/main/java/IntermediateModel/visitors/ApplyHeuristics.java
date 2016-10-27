package intermediateModel.visitors;


import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.BuildEnvironment;
import IntermediateModelHelper.heuristic.definition.*;
import IntermediateModelHelper.indexing.IndexingFile;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexParameter;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import IntermediateModelHelper.envirorment.Env;
import intermediateModel.visitors.interfaces.ParseIM;
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
		build_base_env = BuildEnvironment.getInstance();
	}
	public ApplyHeuristics(BuildEnvironment env) {
		this.build_base_env = env;
	}

	/**
	 * The following method simplify the usage of the current class.
	 * It gets, with a set of predefined heuristics, the time constraints of the given class.
	 * In particular, the list of heuristics that it uses are:
	 * <ul>
	 *     <li>{@link ThreadTime}</li>
	 *     <li>{@link SocketTimeout}</li>
	 *     <li>{@link TimeoutResources}</li>
	 *     <li>{@link TimerType}</li>
	 *     <li>{@link AnnotatedTypes}</li>
	 * </ul>
	 * @param c	Class to analyze
	 * @return	List of time constraints with the predefined set of heuristics
	 */
	public static List<Triplet<String,IASTStm,Class>> getConstraint(ASTClass c){
		return new ArrayList<>();
		/*
		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		ah.analyze(c);
		return ah.getTimeConstraint();
		*/
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
		super.set_class(c);
		Env base = super.createBaseEnv(c);
		IndexingFile indexingFile = new IndexingFile();
		IndexData data = indexingFile.index(c);
		for(IndexParameter p : data.getTimeAttribute()){
			if(base.existVarName(p.getName())){
				IASTVar v = base.getVar(p.getName());
				v.setTimeCritical(true);
			}
		}
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


	public List<Triplet<String, IASTStm, Class>> getTimeConstraint() {
		List<Triplet<String, IASTStm, Class>> out = new ArrayList<>();
		for(SearchTimeConstraint s : strategies){
			out.addAll(s.getTimeConstraint());
		}
		return out;
	}


}
