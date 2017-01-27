package IntermediateModelHelper.indexing;

import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.envirorment.EnvBase;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTImport;
import intermediateModel.structure.ASTVariable;
import intermediateModel.visitors.interfaces.ParseIM;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * This class created the {@link MTMetric} for a given {@link ASTClass}.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingFile extends ParseIM {

	String lastMethodName = "";
	List<String> signatureLastMethodName = new ArrayList<>();
	MTMetric data;
	String anonymousClass = "";
	Stack<String> stackAnonymousClasses = new Stack<>();
	ASTClass _c = null;

	public IndexingFile() {

	}


	/**
	 * Start the indexing of a {@link ASTClass}.
	 * It goes through the methods of it and then through their statements.
	 * @param c	Class to analyze
	 * @return	The index data structure of the class.
	 */
	public MTMetric index(ASTClass c) {
		this._c = c;
		super.set_class(c);
		data = new MTMetric();
		return data;
	}

	private List<String> convertImports(List<ASTImport> imports) {
		List<String> out = new ArrayList<>();
		for(ASTImport i : imports){
			out.add(i.getPackagename());
		}
		return out;
	}

	/**
	 * The following method creates the basic environment for a class.
	 * It goes through the def of all stms and set if variables are time related.
	 * At the end of the execution of the method we know if an attribute is time reletad or not.
	 * <hr>
	 * <b>Efficency Tips</b>: Since we parse the IM we also collect the sync block and time related methods here.
	 * @param c Class to analyze
	 */
	@Override
	protected EnvBase createBaseEnv(ASTClass c){
		super.createBaseEnv(c);
		//check method
		for (IASTMethod m : c.getMethods()) {
			lastMethodName = m.getName();
			signatureLastMethodName = new ArrayList<>();
			for(ASTVariable p : m.getParameters()){
				signatureLastMethodName.add(p.getType());
			}
			Env eMethod = new Env(base_env);
			eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
			super.analyze(m.getStms(), eMethod);
		}
		return base_env;
	}



}