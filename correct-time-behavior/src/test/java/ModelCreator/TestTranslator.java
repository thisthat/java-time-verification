package ModelCreator;

import com.microsoft.z3.Optimize;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.junit.Test;
import slicing.Slice;
import slicing.model.Method;
import smt.TranslateReducedModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by giovanni on 06/07/2017.
 */
public class TestTranslator {

    @Test
    public void TestModel() throws Exception {
        String code = load("translator/code.java");
        String model = read("translator/model.smt");
        ASTClass c = JDTVisitor.parse(code, code.substring(0, code.lastIndexOf("/"))).get(0);
        IASTMethod mm = c.getFirstMethodByName("poll");
        Method m = Slice.slice(c).get(mm);
        TranslateReducedModel translateReducedModel = new TranslateReducedModel();
        Optimize opt = translateReducedModel.convert(m);
        System.out.println(opt);
        assertEquals(model, opt.toString());
    }

    private String load(String file) {
        return TestTranslator.class.getClassLoader().getResource(file).getFile();
    }

    private String read(String file) throws IOException {
        Path p = Paths.get(load(file));
        List<String> lines = Files.readAllLines(p);
        StringBuilder sb = new StringBuilder();
        for(String l : lines) {
            l = l.trim();
            if(l.startsWith(";")) continue;
            if(l.startsWith("\n")) continue;
            if(l.equals("")) continue;
            sb.append(l + "\n");
        }
        return sb.toString();
    }

}
