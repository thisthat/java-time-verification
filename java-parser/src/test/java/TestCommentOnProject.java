import org.antlr.v4.runtime.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import parser.grammar.Java8CommentSupportedLexer;
import parser.grammar.Java8CommentSupportedParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by giovanni (@thisthatDC) on 18/03/16.
 */
public class TestCommentOnProject {

    static int nException = 0;
    static List<String> errors = new ArrayList<String>();
    static int line_number = 0;
    BufferedWriter out;

    public void testGrammarByName(String name) throws Exception {
        InputStream in = null;
        line_number = 0;
        Path file = Paths.get(name);
        try {
            in = Files.newInputStream(file);
        }
        catch(Exception e){
            System.out.println(name);
        }
        Java8CommentSupportedLexer l = new Java8CommentSupportedLexer(new ANTLRInputStream(in));
        Java8CommentSupportedParser p = new Java8CommentSupportedParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                line_number = line;
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        try {
            ParserRuleContext t = p.compilationUnit();
        }
        catch(Exception e){
            String err = "Error parsing: " + name + " -- " + line_number + "\n";
            //errors.add(err);
            System.err.println(err);
            out.write(err);
            out.flush();
            throw e;//new Exception("Test failed for bhu reason");
        }
        finally {
            l = null;
            p = null;
            System.gc();
        }
    }

    @Test
    public void testAll() throws Exception {

        String lastOne = "/Users/giovanni/repository/java-parser/testProject/spark-1.6.1/unsafe/src/testenvirorments.test_antrl/java/org/apache/spark/unsafe/types/UTF8StringSuite.java";
        //Log to file
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
        File file = new File("errors-" + dateFormat.format(date) + ".log") ;
        out = new BufferedWriter(new FileWriter(file));


        String base_path = System.getProperty("user.dir");
        base_path += "/testProject";
        File dir = new File(base_path);
        String[] filter = {"java"};
        Collection<File> files = FileUtils.listFiles(
                dir,
                filter,
                true
        );
        Iterator i = files.iterator();
        boolean reached = false;
        while (i.hasNext()) {
            String filename = ((File)i.next()).getAbsolutePath();
            if(!reached && !filename.equals(lastOne))
                continue;
            reached = true;
            try {
                testGrammarByName(filename);
                String s = "Correctly parsed:" + filename;
                System.out.println(s);
            } catch (Exception e) {
                //e.printStackTrace();
                nException++;
            }
        }

        if(nException > 0)
            throw new Exception("Test phase not passed! " + nException + " errors.");
        System.err.println(Arrays.toString(errors.toArray()));
        out.close();
    }


    private void walk( String path ) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
            }
            else {
                String filename = f.getAbsoluteFile().toString();
                if(getExt(filename).equals("java")){
                    //java file, let's testenvirorments.test_antrl it!
                    try {
                        testGrammarByName(filename);
                        String s = "Correctly parsed:" + filename;
                        System.out.println(s);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        nException++;
                    }
                }
            }
        }
    }

    private String getExt(String file){
        String tmp = file.substring(file.lastIndexOf(".") + 1);
        //System.out.println(tmp + " :: " + file);
        return tmp;
    }


}
