import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.interfaces.IASTStm;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTMethod;
import IntermediateModel.structure.ASTRE;
import IntermediateModel.structure.expression.NotYetImplemented;
import IntermediateModel.visitors.CreateIntemediateModel;
import IntermediateModel.visitors.DefualtASTREVisitor;
import XALConversion.util.Pair;
import com.google.common.collect.Iterators;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ObjectEqualityComparator;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
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
				a = new Java2AST(filename, Java2AST.VERSION.Java_7, true);
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
			//check RE
			for(ASTClass c : sv.listOfClasses){
				System.out.println(c);
				for(IASTMethod m : c.getMethods()){
					for(IASTStm stm : m.getStms()){
						if(stm instanceof ASTRE){
							String finalFilename = filename;
							((ASTRE) stm).getExpression().visit(new DefualtASTREVisitor(){
								@Override
								public void enterElse(NotYetImplemented elm) {
									reNotParsed.add(new Pair(finalFilename,elm));
								}
							});
						}
					}
				}
			}
			filename = null;
			//a = null;
			ast = null;
			walker = null;
			sv = null;
		}
	}


	//@Before
	public void setUpJDT() throws Exception {
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
			ASTNode result = a.getContextJDT();
			n++;
			double perc = Math.floor(1000 * ( (double)n/ (double)tot)) / 1000;
			System.out.println("[" + perc  +"] File parsed: " + filename);

		}
	}

	@org.junit.Test
	public void TestAllFilesParsedCorrectly() throws Exception {
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
		}
		System.err.println("-- List of Files Skiped --");
		for(String f : fileToSkip){
			System.err.println(f);
		}
		System.err.println("-- End List of Files Skiped --");
		assertEquals(fileNotParsed.size(),0);
		assertEquals(reNotParsed.size(),0);

	}

	@Test
	public void TestLambdas() throws Exception {
		String filename = getClass().getResource("testLambdas.java").getPath();
		Java2AST a = new Java2AST(filename, Java2AST.VERSION.JDT, true);
		ASTNode result = a.getContextJDT();
	}
}
