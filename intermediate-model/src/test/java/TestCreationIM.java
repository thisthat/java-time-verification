import intermediateModel.interfaces.IASTRE;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.expression.NotYetImplemented;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.DefualtASTREVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.javatuples.Pair;
import org.junit.Test;
import parser.Java2AST;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestCreationIM {

	private static final boolean __report = false;


	List<Pair<String,String>> fileNotParsed = new ArrayList<>();
	List<Pair<String,NotYetImplemented>>  reNotParsed   = new ArrayList<>();
	List<String> fileToSkip = new ArrayList<>();
	private long totalREXP = 0;

	public void setUpJDT(String folder) throws Exception {
		String base_path = getClass().getResource(folder).getPath();
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();

			Java2AST a;
			try {
				a = new Java2AST(filename, true);
			}
			catch (Exception e){
				fileToSkip.add(filename);
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result, filename);
			//System.err.println("Processing: " + filename);
			result.accept(v);
			for(ASTClass c : v.listOfClasses){
				c.visit(new DefaultASTVisitor(){
					@Override
					public void enterASTRE(ASTRE elm) {
						elm.getExpression().visit(new DefualtASTREVisitor(){
							@Override
							public void enterAll(IASTRE elm) {
								totalREXP++;
							}

							@Override
							public void enterNotYetImplemented(NotYetImplemented elm) {
								reNotParsed.add(new Pair(filename,elm));
							}
						});
					}

				});
			}
		}
	}

	@Test
	public void TestVuze() throws Exception {
		setUpJDT("vuze");
		if(__report && fileNotParsed.size() > 0){
			System.err.println("-- List file not correctly parsed --");
			for(Pair<String,String> f : fileNotParsed){
				System.err.println("[" + f.getValue0() + "]");
				System.err.println(f.getValue1());
			}
		}
		if(__report && reNotParsed.size() > 0){
			System.err.println("-- List RE not correctly parsed --");
			for(Pair<String,NotYetImplemented> re : reNotParsed){
				System.err.println("[" + re.getValue0() + "]");
				System.err.println(re.getValue1());
			}
			System.err.println("Total REXP in the project: " + totalREXP);
			System.err.println("Total REXP not parsed the project: " + reNotParsed.size());
			System.err.println("% " + 100*((double)reNotParsed.size() / (double)totalREXP));
		}
		assertEquals(0,fileNotParsed.size());
		assertEquals(0,reNotParsed.size());

	}

}
