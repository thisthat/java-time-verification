package IntermediateModelHelper.indexing;

import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ParseIM;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */


public class IndexingFile extends ParseIM {

	Env base_env = new Env();
	BuildEnvirormentClass build_base_env;
	{
		build_base_env = new BuildEnvirormentClass(base_env);
	}
	String lastMethodName = "";
	IndexData data;

	public IndexData index(ASTClass c) {
		data = new IndexData();
		data.setClassName(c.getName());
		data.setClassPackage(c.getPackageName());
		data.setExtendedType(c.getExtendClass());
		data.setInterfacesImplemented(c.getImplmentsInterfaces());
		//collecting the names of methods and sync method
		for(IASTMethod m : c.getMethods()){
			data.addMethod(prepareOutput(m));
			if(m instanceof ASTMethod){
				if(((ASTMethod) m).isSyncronized()){
					data.addSyncMethod(prepareOutput(m));
				}
			}
		}
		createBaseEnv(c);
		return data;
	}

	private IndexMethod prepareOutput(IASTMethod m) {
		IndexMethod im = new IndexMethod();
		im.setName(m.getName());
		im.setPackageName(data.getClassPackage());
		im.setParameters(IndexMethod.convertPars(m.getParameters()));
		im.setExceptionsThrowed(m.getExceptionsThrowed());
		im.setStart(((IASTStm)m).getStart());
		im.setEnd(((IASTStm)m).getEnd());
		im.setLine(((IASTStm)m).getLine());
		im.setConstructor( m instanceof ASTConstructor );
		im.setSync( !im.isConstructor() && ((ASTMethod) m).isSyncronized() );
		return im;
	}

	private IndexSyncBlock prepareOutput(ASTSynchronized m) {
		IndexSyncBlock is = new IndexSyncBlock();
		is.setPackageName(data.getClassPackage());
		is.setClassName(data.getClassName());
		is.setMethodName(lastMethodName);
		is.setExpr(m.getExpr().getCode());
		is.setStart(m.getStart());
		is.setEnd(m.getEnd());
		is.setLine(m.getLine());
		return is;
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
		data.addSyncBlock(prepareOutput(elm));
	}

	@Override
	protected void analyzeASTReturn(ASTReturn elm, Env env) {
		/*
		ASTRE re = elm.getExpr();
		if(re != null && re.getExpression() != null && //sanity checks
				BuildEnvirormentClass.checkIt(re.getExpression(), env)){
			listOfTimedMethods.add(lastMethodName);
		}
		*/
	}
}