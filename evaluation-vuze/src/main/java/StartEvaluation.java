import IntermediateModelHelper.heuristic.*;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;
import parser.exception.ParseErrorsException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class StartEvaluation {

	public static void main(String args[]) throws IOException, ParseErrorsException {
		if(args.length < 1){
			System.out.println("You should pass as parameter the directory of the project to analyze");
			return;
		}
		new StartEvaluation().run(args[0]);
	}

	public void run(String base_path) throws IOException, ParseErrorsException {
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();

		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);
		int n = 0;
		int n_h = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();

			Java2AST a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result);
			result.accept(v);
			for(ASTClass c : v.listOfClasses){

				ah.analyze(c);
				if(ah.getTimeConstraint().size() > 0) {
					n++;
					n_h += ah.getTimeConstraint().size();
					String s = Arrays.toString(ah.getTimeConstraint().toArray());
					System.out.println("[" + filename + "]");
					System.out.println(s);
					System.out.println("__________");
				}
			}
		}
		System.out.println("Found " + n_h + " in " + n + "files");
	}
}
