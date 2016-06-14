import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;

/**
 * Created by giovanni (@thisthatDC) on 18/03/16.
 */
public class TestFromAST2Java {

    String[] test_files;
    @Before
    public void initTest(){
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/";
        File dir = new File(base_path);
        String[] filter = {"java"};
        Collection<File> files = FileUtils.listFiles(
                dir,
                filter,
                true
        );
        test_files = new String[files.size()];
        Iterator i = files.iterator();
        int j = 0;
        while (i.hasNext()) {
            String filename = ((File)i.next()).getAbsolutePath();
            test_files[j++] = filename;
        }
    }

    @Test(expected = Exception.class)
    public void testParseError() throws Exception {
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/OnlyMethod.java";
        System.out.println(base_path);
        Java2AST f2j = new Java2AST(base_path);
        f2j.convertToAST(Java2AST.VERSION.Java_8);
    }

    @Test
    public void testCorrectParsing() throws Exception {
        String base_path = System.getProperty("user.dir");
        base_path += "/src/main/resources/HelloWorld.java";
        System.out.println(base_path);
        Java2AST f2j = new Java2AST(base_path);
        f2j.convertToAST(Java2AST.VERSION.Java_8);
    }

    //@Test
    public void testCorrectRoundTrip() throws Exception {
        String[] test = new String[test_files.length - 1];
        int j = 0;
        for(int i = 0; i < test_files.length; i++){
            if(!test_files[i].contains("Only")){
                test[j++] = test_files[i];
            }
        }
        for(String file : test){
            System.out.println(file);
            Java2AST f2j = new Java2AST(file, Java2AST.VERSION.Java_8, true);
            File file1 = new File(file);
            //File file2 = f2j.convert2Java();
            /* TODO : Remove comments from both files to correctly check when they are equal
            assertEquals("The files differ!",
                    readFileToString(file1, "utf-8"),
                    readFileToString(file2, "utf-8")
            );
            */
        }

    }

}
