package calculation;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTSynchronized;
import IntermediateModel.visitors.DefaultASTVisitor;
import IntermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CountClassWSyncBlocks {


	String _NAMES_[] = {"activemq","airavata","jetty","vuze","wildfly-core"};
	String base_path = "/Users/giovanni/repository/java-xal/project_eval/";

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		CountClassWSyncBlocks eval = new CountClassWSyncBlocks();
		eval.run();
	}

	private void run() throws FileNotFoundException, UnsupportedEncodingException {

		for(int i = 0; i < _NAMES_.length; i++){
			long start = System.currentTimeMillis();
			String name = _NAMES_[i];
			System.out.println("\n[Working with: " + name + "]");
			work(name);
			long end = System.currentTimeMillis();
			System.out.println((end - start));
		}


	}

	private void work(String name) {
		File dir = new File(base_path + name);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		int n_file_m = 0;
		int n_class = 0;
		int n_class_m = 0;
		int total = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(filename.contains("/test")){
				continue;
			}
			List<ASTClass> classes = JDTVisitor.parse(filename);
			//pp filename
			final boolean[] flag = {false};
			boolean atLeastOne = false;
			boolean atLeastOneMethod = false;
			boolean flagMethod = false;
			for(ASTClass c : classes){
				flag[0] = false;
				flagMethod = false;
				c.visit(new DefaultASTVisitor() {
					@Override
					public void enterASTSynchronized(ASTSynchronized elm) {
						flag[0] = true;
					}
				});
				if(flag[0]){
					atLeastOne = true;
					n_class++;
				}
				for(IASTMethod m : c.getMethods()){
					if(m instanceof ASTMethod && ((ASTMethod) m).isSyncronized()){
						flagMethod = true;
					}
				}
				if(flagMethod){
					atLeastOneMethod = true;
					n_class_m++;
				}
			}
			if(atLeastOne)
				n_file++;
			if(atLeastOneMethod)
				n_file_m++;

			total++;
		}
		System.out.println("Classes w/ sync blocks: " + n_class);
		System.out.println("Classes w/ sync methods: " + n_class_m);
		System.out.println("Files w/ sync blocks:   " + n_file);
		System.out.println("Files w/ sync methods:   " + n_file_m);
		//System.out.println("Total files analyzed:   " + total);
	}
}
