import IntermediateModel.interfaces.IASTRE;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.NotYetImplemented;
import IntermediateModel.visitors.CreateIntemediateModel;
import IntermediateModel.visitors.DefaultASTVisitor;
import IntermediateModel.visitors.DefualtASTREVisitor;
import IntermediateModel.visitors.JDTVisitor;
import XALConversion.util.Pair;
import com.google.common.collect.Iterators;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.*;
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

	private static final int __MAX_LINES__ = 2000;
	List<Pair<String,String>> fileNotParsed = new ArrayList<>();
	List<Pair<String,NotYetImplemented>>  reNotParsed   = new ArrayList<>();
	List<String> fileToSkip = new ArrayList<>();
	private long totalREXP = 0;


	private static int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}


	//@Before
	public void setUp() throws Exception {
		String base_path = getClass().getResource("vuze").getPath();
		File dir = new File(base_path);
		String[] filter = {"java"};
		Collection<File> files = FileUtils.listFiles(
				dir,
				filter,
				true
		);
		Iterator i = files.iterator();
		int tot = Iterators.size(i);
		i = files.iterator();
		int n = 0;
		while (i.hasNext()) {
			String filename = ((File)i.next()).getAbsolutePath();

			Java2AST a;
			try {
				a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			}
			catch (Exception e){
				fileToSkip.add(filename);
				continue;
			}
			ParserRuleContext ast = a.getContext();
			ParseTreeWalker walker = new ParseTreeWalker();
			CreateIntemediateModel sv = new CreateIntemediateModel();
			n++;
			double perc = Math.floor(1000 * ( (double)n/ (double)tot)) / 1000;
			System.out.println("[" + perc  +"] File parsed: " + filename);
			try {
				walker.walk(sv, ast);
			} catch(Exception e){
				fileNotParsed.add(new Pair(filename,e));
			}
			filename = null;
			//a = null;
			ast = null;
			walker = null;
			sv = null;
		}
	}



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
				a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
			}
			catch (Exception e){
				fileToSkip.add(filename);
				continue;
			}
			CompilationUnit result = a.getContextJDT();
			JDTVisitor v = new JDTVisitor(result);
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
		if(fileNotParsed.size() > 0){
			System.err.println("-- List file not correctly parsed --");
			for(Pair<String,String> f : fileNotParsed){
				System.err.println("[" + f.getFirst() + "]");
				System.err.println(f.getSecond());
			}
		}
		if(reNotParsed.size() > 0){
			System.err.println("-- List RE not correctly parsed --");
			for(Pair<String,NotYetImplemented> re : reNotParsed){
				System.err.println("[" + re.getFirst() + "]");
				System.err.println(re.getSecond());
			}
			System.err.println("Total REXP in the project: " + totalREXP);
			System.err.println("Total REXP not parsed the project: " + reNotParsed.size());
			System.err.println("% " + 100*((double)reNotParsed.size() / (double)totalREXP));
		}
		System.err.println("-- List of Files Skiped --");
		for(String f : fileToSkip){
			System.err.println(f);
		}
		System.err.println("-- End List of Files Skiped --");
		assertEquals(0,fileNotParsed.size());
		assertEquals(0,reNotParsed.size());

	}

	@Test
	public void TestAllFilesParsedCorrectly() throws Exception {
		setUpJDT("changevis");
		if(fileNotParsed.size() > 0){
			System.err.println("-- List file not correctly parsed --");
			for(Pair<String,String> f : fileNotParsed){
				System.err.println("[" + f.getFirst() + "]");
				System.err.println(f.getSecond());
			}
		}
		if(reNotParsed.size() > 0){
			System.err.println("-- List RE not correctly parsed --");
			for(Pair<String,NotYetImplemented> re : reNotParsed){
				System.err.println("[" + re.getFirst() + "]");
				System.err.println(re.getSecond());
			}
			System.err.println("Total REXP in the project: " + totalREXP);
			System.err.println("Total REXP not parsed the project: " + reNotParsed.size());
			System.err.println("% " + 100*((double)reNotParsed.size() / (double)totalREXP));
		}
		System.err.println("-- List of Files Skiped --");
		for(String f : fileToSkip){
			System.err.println(f);
		}
		System.err.println("-- End List of Files Skiped --");
		assertEquals(0,fileNotParsed.size());
		assertEquals(0,reNotParsed.size());

	}

}
