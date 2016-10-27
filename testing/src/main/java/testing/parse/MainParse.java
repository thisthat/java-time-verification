package testing.parse;

import IntermediateModelHelper.CheckExpression;
import IntermediateModelHelper.envirorment.Env;
import IntermediateModelHelper.envirorment.EnvBase;
import IntermediateModelHelper.indexing.structure.*;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.*;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.interfaces.ParseIM;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import testing.indexing.MainSyncBlockHugeClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MainParse {

	public class IndexingFile extends ParseIM {

		String lastMethodName = "";
		IndexData data;

		public IndexingFile() {
		}


		/**
		 * Start the indexing of a {@link ASTClass}.
		 * It force to delete the index structure from the DB and recreate it.
		 *
		 * @param c	Class to analyse.
		 * @return	The index data structure of the class.
		 */
		public IndexData index(ASTClass c){
			return index(c, true);
		}
		/**
		 * Start the indexing of a {@link ASTClass}.
		 * It goes through the methods of it and then through their statements.
		 * @param c	Class to analyze
		 * @param forceReindex flag to force the recreation of the index
		 * @return	The index data structure of the class.
		 */
		public IndexData index(ASTClass c, boolean forceReindex) {
			data = new IndexData();
			data.setClassName(c.getName());
			data.setClassPackage(c.getPackageName());
			data.setImports(convertImports(c.getImports()));
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
			// add the attributes time related
			Env finalEnv = createBaseEnv(c);
			for(IASTVar v : finalEnv.getVarList()){
				if(!v.isTimeCritical()){
					continue;
				}
				IndexParameter p = new IndexParameter();
				p.setType(v.getType());
				p.setName(v.getName());
				data.getTimeAttribute().add(p);
			}
			//mongo.add(data);
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
		 * Convert an {@link IASTMethod} Object to {@link IndexMethod} structure
		 * @param m	Method to convert
		 * @return	Its representation in the {@link IndexMethod} structure.
		 */
		private IndexMethod prepareOutput(IASTMethod m) {
			IndexMethod im = new IndexMethod();
			im.setName(m.getName());
			im.setPackageName(data.getClassPackage());
			im.setFromClass(data.getClassName());
			im.setParameters(IndexMethod.convertPars(m.getParameters()));
			im.setExceptionsThrowed(m.getExceptionsThrowed());
			im.setStart(((IASTStm)m).getStart());
			im.setEnd(((IASTStm)m).getEnd());
			im.setLine(((IASTStm)m).getLine());
			im.setConstructor( m instanceof ASTConstructor);
			im.setSync( !im.isConstructor() && ((ASTMethod) m).isSyncronized() );
			im.setReturnType(m.getReturnType());
			return im;
		}

		/**
		 * Convert an {@link ASTSynchronized} Object to {@link IndexSyncBlock} structure
		 * @param m	Synchronized block to convert
		 * @param e Environment of the Synchronized block
		 * @return	Its representation in the {@link IndexMethod} structure.
		 */
		private IndexSyncBlock prepareOutput(ASTSynchronized m, Env e) {
			IndexSyncBlock is = new IndexSyncBlock();
			is.setPackageName(data.getClassPackage());
			is.setName(data.getClassName());
			is.setMethodName(lastMethodName);
			is.setExpr(m.getExpr().getCode());
			is.setStart(m.getStart());
			is.setEnd(m.getEnd());
			is.setLine(m.getLine());
			is.setSyncVar( new IndexEnv(e).getVar(is.getExpr()));
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
		protected EnvBase createBaseEnv(ASTClass c){
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
			System.out.println(elm.toString());
			System.out.println("------");
			//data.addSyncBlock(prepareOutput(elm, env));
		}

		@Override
		protected void analyzeASTReturn(ASTReturn elm, Env env) {

			ASTRE re = elm.getExpr();
			if(re != null && re.getExpression() != null && //sanity checks
					CheckExpression.checkIt(re.getExpression(), env)){
				data.getListOfTimedMethods().add(lastMethodName);
			}

		}
	}

	public static void main(String[] args){
		new MainParse().run();
	}

	public void run(){
		String base_path = MainSyncBlockHugeClass.class.getClassLoader().getResource("BuddyPluginViewBetaChat.java").getPath();
		Java2AST a = null;
		try {
			a = new Java2AST(base_path, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		CompilationUnit result = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(result, base_path);
		result.accept(v);
		for(ASTClass c : v.listOfClasses){
			MainParse.IndexingFile indexingFile = new IndexingFile();
			indexingFile.index(c);
		}
	}
}
