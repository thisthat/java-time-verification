package firstEvalPCFG;

import IntermediateModelHelper.indexing.IndexingProject;
import IntermediateModelHelper.indexing.mongoConnector.MongoConnector;
import IntermediateModelHelper.indexing.mongoConnector.MongoOptions;
import PCFG.structure.PCFG;
import PCFG.visitors.IM2PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.*;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class StartEvaluationPCFG {

	private static final String _EVALUATION_NAME = "vuze_eval";
	List<ASTClass> listOfClasses = new ArrayList<>();
	PrintWriter writer;

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		double time = new Date().getTime();
		new StartEvaluationPCFG().run();
		System.out.println("Total Time: [" + (new Date().getTime() - time) / 1000 + "s]" );
	}

	private void run() throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter("result" + this.getClass().getSimpleName().toString() + ".csv", "UTF-8");
		writer.println("package-out;class-name-out;method-name-out;package-in;class-name-in;method-name-in;time-constraint;number-sync;total;");
		MongoOptions.getInstance().setDbName(_EVALUATION_NAME);
		String dir = StartEvaluationPCFG.class.getClassLoader().getResource("vuze/index.txt").getPath();
		dir = dir.substring(0, dir.lastIndexOf("/") + 1);
		indexProject(dir);
		MongoConnector mongo = MongoConnector.getInstance(_EVALUATION_NAME);
		mongo.ensureIndexes();
		System.out.println("Project indexed");
		computeAllClasses(dir);
		System.out.println("Classes generated");
		compare();
		writer.close();
	}

	private void compare() { //all possible pairs
		//for(ASTClass cOut : listOfClasses){
		//	for(ASTClass cIn : listOfClasses){
		double max = listOfClasses.size();
		int n = 0;
		while(listOfClasses.size() > 0) {
			ASTClass cOut = listOfClasses.get(0);
			n++;
			double perc = (double) n / max * 100;
			for(int j = 0; j < listOfClasses.size(); j++){
				ASTClass cIn = listOfClasses.get(j);
				System.out.println( "[" + perc + "] Processing: " + cOut.getPackageName() + ":" + cOut.getName() + " vs " + cIn.getPackageName() + ":" + cIn.getName());
				for(IASTMethod mOut : cOut.getMethods()){
					for(IASTMethod mIn : cIn.getMethods()){
						compare(cOut, mOut, cIn, mIn);
					}
				}
				//System.out.println(cOut.getName() + "-" + cIn.getName());
			}
			listOfClasses.remove(cOut);
		}
	}

	private void compare(ASTClass cOut, IASTMethod mOut, ASTClass cIn, IASTMethod mIn) {
		IM2PCFG p = new IM2PCFG();
		p.addClass(cOut, mOut.getName(), false);
		p.addClass(cIn , mIn.getName(), false);
		PCFG graph = p.buildPCFG();
		int timeConstraint = p.getConstraintsSize();
		int numberSync = graph.getESync().size();
		String pkgOut = cOut.getPackageName();
		String pkgIn = cIn.getPackageName();
		//System.out.println(cOut.getName() + ":" + mOut.getName() + "_vs_" + cIn.getName() + ":" + mIn.getName() + " = " + timeConstraint + "," + numberSync);
		writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;", pkgOut, cOut.getName(), mOut.getName(), pkgIn, cIn.getName(), mIn.getName(), timeConstraint, numberSync, (timeConstraint + numberSync) ));
		writer.flush();
	}

	private void computeAllClasses(String _dir) {
		File dir = new File(_dir);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			Java2AST a = null;
			try {
				a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (ParseErrorsException e) {
				e.printStackTrace();
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			listOfClasses.addAll(v.listOfClasses);
		}
	}

	private void indexProject(String dir) {
		IndexingProject indexing = new IndexingProject(_EVALUATION_NAME);
		indexing.indexProject(dir, true);
	}
}
