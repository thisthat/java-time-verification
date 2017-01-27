package calculation;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class CountTimeCnst {

	public static void main(String... args){
		String[] projects = {"activemq","airavata", "jetty", "vuze", "wildfly-core"};
		for(String p : projects){
			System.out.append(p);
			compute(p);
			System.out.println();
		}
	}

	private static void compute(String project){
		String base_path = "/Users/giovanni/repository/sources/" + project;
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int n_file = 0;
		int n_test = 0;
		int cnt = 0;
		int t_cnt = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();
			if(filename.contains("/test")){
				n_test++;
				for(ASTClass c : JDTVisitor.parse(filename, base_path)){
					t_cnt += ApplyHeuristics.getConstraint(c).size();
				}
			} else {
				n_file++;
				//pp filename
				for(ASTClass c : JDTVisitor.parse(filename, base_path)){
					cnt += ApplyHeuristics.getConstraint(c).size();
				}
			}
		}
		System.out.println("Classes: " + n_file);
		System.out.println("Tests  : " + n_test);
		System.out.println("Tot    : " + (n_test + n_file));
		System.out.println("Time Cnst: " + cnt);
		System.out.println("Time Cnst Tests: " + t_cnt);
	}


}
