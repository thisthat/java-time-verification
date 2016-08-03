package IntermediateModelHelper.indexing;

import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.BuildEnvirormentClass;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.indexing.structure.IndexData;
import IntermediateModelHelper.indexing.structure.IndexEnv;
import IntermediateModelHelper.indexing.structure.IndexMethod;
import IntermediateModelHelper.indexing.structure.IndexSyncBlock;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.visitors.ParseIM;

/**
 *
 * This class created the {@link IndexData} for a given {@link ASTClass}.
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class IndexingFile extends ParseIM {

	String lastMethodName = "";
	IndexData data;

	/**
	 * Start the indexing of a {@link ASTClass}.
	 * It goes through the methods of it and then through their statements.
	 * @param c	Class to analyze
	 * @return	The index data structure of the class.
	 */
	public IndexData index(ASTClass c) {
		data = new IndexData();
		data.setClassName(c.getName());
		data.setClassPackage(c.getPackageName());
		String fullname = "";
		if(c.getPackageName().trim().equals("")){
			fullname = c.getName();
		} else {
			fullname = c.getPackageName() + "." + c.getName();
		}
		data.setFullName(fullname);
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
		im.setType(data.getClassName());
		im.setParameters(IndexMethod.convertPars(m.getParameters()));
		im.setExceptionsThrowed(m.getExceptionsThrowed());
		im.setStart(((IASTStm)m).getStart());
		im.setEnd(((IASTStm)m).getEnd());
		im.setLine(((IASTStm)m).getLine());
		im.setConstructor( m instanceof ASTConstructor );
		im.setSync( !im.isConstructor() && ((ASTMethod) m).isSyncronized() );
		im.setReturnType(m.getReturnType());
		return im;
	}

	private IndexSyncBlock prepareOutput(ASTSynchronized m, Env e) {
		IndexSyncBlock is = new IndexSyncBlock();
		is.setPackageName(data.getClassPackage());
		is.setClassName(data.getClassName());
		is.setMethodName(lastMethodName);
		is.setExpr(m.getExpr().getCode());
		is.setStart(m.getStart());
		is.setEnd(m.getEnd());
		is.setLine(m.getLine());
		is.setEnv( new IndexEnv(e));
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
	@Override
	protected Env createBaseEnv(ASTClass c){
		super.createBaseEnv(c);
		//check method
		for (IASTMethod m : c.getMethods()) {
			lastMethodName = m.getName();
			Env eMethod = new Env(base_env);
			eMethod = CheckExpression.checkPars(m.getParameters(), eMethod);
			super.analyze(m.getStms(), eMethod);
		}
		return base_env;
	}


	@Override
	protected void analyzeASTRE(ASTRE r, Env env) {
		CheckExpression.checkRE(r, env);
	}

	@Override
	protected void analyzeASTSynchronized(ASTSynchronized elm, Env env) {
		data.addSyncBlock(prepareOutput(elm, env));
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