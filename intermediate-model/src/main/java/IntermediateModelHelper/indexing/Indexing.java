package IntermediateModelHelper.indexing;

import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.reducedstructure.IndexMethod;
import com.google.common.annotations.Beta;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTNewObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Indexing extends ParseIM {
	List<IndexMethod> listOfMethods = new ArrayList<>();
	@Beta
	List<String> listOfTimedMethods = new ArrayList<>();
	List<IndexMethod> listOfSyncMethods = new ArrayList<>();
	List<ASTSynchronized> listOfSyncBlocks = new ArrayList<>();
	Env base_env = new Env();
	BuildEnvirormentClass build_base_env;
	{
		build_base_env = new BuildEnvirormentClass(base_env);
	}

	String lastMethodName = "";
	String classPackage = "";

	public Indexing(ASTClass c) {
		classPackage = c.getPackageName();
		//collecting the names of methods and sync method
		for(IASTMethod m : c.getMethods()){
			listOfMethods.add(prepareOutput(m));
			if(m instanceof ASTMethod){
				if(((ASTMethod) m).isSyncronized()){
					listOfSyncMethods.add(prepareOutput(m));
				}
			}
		}
		createBaseEnv(c);
	}

	private IndexMethod prepareOutput(IASTMethod m) {
		IndexMethod im = new IndexMethod();
		im.setName(m.getName());
		im.setPackageName(classPackage);
		im.setParameters(m.getParameters());
		im.setExceptionsThrowed(m.getExceptionsThrowed());
		im.setStart(((IASTStm)m).getStart());
		im.setEnd(((IASTStm)m).getEnd());
		im.setLine(((IASTStm)m).getLine());
		im.setConstructor( m instanceof ASTConstructor );
		im.setSync( !im.isConstructor() && ((ASTMethod) m).isSyncronized() );
		return im;
	}

	public List<IndexMethod> getListOfMethods() {
		return listOfMethods;
	}

	@Beta
	public List<String> getListOfTimedMethods() {
		return listOfTimedMethods;
	}

	public List<IndexMethod> getListOfSyncMethods() {
		return listOfSyncMethods;
	}

	public List<ASTSynchronized> getListOfSyncBlocks() {
		return listOfSyncBlocks;
	}

	/**
	 * The following method creates the basic environment for a class.
	 * It goes through the def of all stms and set if variables are time related.
	 * At the end of the execution of the method we know if an attribute is time reletad or not.
	 * <hr>
	 * <b>Efficency Tips</b>: Since we parse the IM we also collect the sync block and time related methods here.
	 * @param c Class to analyze
	 */
	private void createBaseEnv(ASTClass c){
		build_base_env.buildEnvClass(c);
		//check static
		for (ASTStatic s : c.getStaticInit()) {
			analyze(s.getStms(), base_env);
		}
		//check method
		for (IASTMethod m : c.getMethods()) {
			lastMethodName = m.getName();
			Env eMethod = new Env(base_env);
			eMethod = build_base_env.checkPars(m.getParameters(), eMethod);
			super.analyze(m.getStms(), eMethod);
		}
	}


	@Override
	protected void analyzeASTRE(ASTRE r, Env env) {
		build_base_env.checkRE(r, env);
	}

	@Override
	protected void analyzeASTSynchronized(ASTSynchronized elm, Env env) {
		listOfSyncBlocks.add(elm);
	}

	@Override
	protected void analyzeASTReturn(ASTReturn elm, Env env) {
		ASTRE re = elm.getExpr();
		if(re != null && re.getExpression() != null && //sanity checks
				BuildEnvirormentClass.checkIt(re.getExpression(), env)){
			listOfTimedMethods.add(lastMethodName);
		}
	}
}