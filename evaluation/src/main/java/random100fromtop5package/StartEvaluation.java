package random100fromtop5package;

import intermediateModelHelper.heuristic.definition.*;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Triplet;
import timeannotation.parser.Java2AST;

import java.io.*;
import java.util.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class StartEvaluation {

	private static final String __dir = "top5package";
	private static final String __random = "evaluation-vuze/csvs/random100/files_" + __dir + ".csv";

	public static void main(String args[]) throws IOException, InterruptedException {
		String path = "";
		if(args.length < 1){
			System.err.println("You should pass as parameter the directory of the project to analyze");
			System.err.println("A default one is used");
			Class c = StartEvaluation.class;
			path = c.getClassLoader().getResource(__dir).getPath();
		} else {
			path = args[0];
		}
		new StartEvaluation().run(path);
	}

	public void run(String base_path) throws IOException, InterruptedException {
		File f = new File(__random);
		if(!f.exists()){
			calculateFiles(base_path);
		}
		parse();
	}

	private void calculateFiles(String base_path) throws IOException, InterruptedException {
		PrintWriter writer = new PrintWriter(__random, "UTF-8");
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		List<String> listJavafiles = new ArrayList<>();
		List<String> randomJavafiles = new ArrayList<>();
		while (i.hasNext()) {
			String filename = ((File) i.next()).getAbsolutePath();
			listJavafiles.add(filename);
		}
		int max = listJavafiles.size() > 100 ? 100 : listJavafiles.size();
		Random r = new Random();
		while(max > 0){
			int index = r.nextInt(listJavafiles.size());
			if(!randomJavafiles.stream().anyMatch(f -> f.equals(listJavafiles.get(index)))){
				writer.println(listJavafiles.get(index));
				randomJavafiles.add(listJavafiles.get(index));
				max--;
			}
		}
		writer.close();
	}

	private void parse() throws IOException {


		List<String> listJavafiles = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader(new File(__random)));

		String line = null;
		while ((line = br.readLine()) != null) {
			listJavafiles.add(line);
		}

		ApplyHeuristics ah = new ApplyHeuristics();
		ah.subscribe(ThreadTime.class);
		ah.subscribe(SocketTimeout.class);
		ah.subscribe(TimeoutResources.class);
		ah.subscribe(TimerType.class);
		ah.subscribe(AnnotatedTypes.class);

		//Storage file
		PrintWriter writer = new PrintWriter("evaluation-vuze/csvs/random100/eval_" + __dir + ".csv", "UTF-8");
		writer.println("package;file;line;code;type;correct");

		for(String filename : listJavafiles){
			Java2AST a = new Java2AST(filename,  true);
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			result.accept(v);
			//pp filename
			filename = filename.substring( filename.indexOf("/" + __dir) );
			//listJavafiles.add(filename);
			for(ASTClass c : v.listOfClasses){
				ah.analyze(c);
				if(ah.getTimeConstraint().size() > 0) {
					String s = Arrays.toString(ah.getTimeConstraint().toArray());
					for(Triplet<String, IASTStm ,Class> t : ah.getTimeConstraint()){
						//pp code
						String code = t.getValue1().getCode();
						code = code.replace("\n", "");
						code = code.replace("\r", "");
						code = code.replace("\t", " ");
						code = code.replace(";", "");
						writer.println(String.format("%s;%s;%s;%s;%s", c.getPackageName(), filename, t.getValue0(), code, t.getValue2().getSimpleName() ));
					}
				}
			}
		}

		//close file
		writer.close();

	}
}
