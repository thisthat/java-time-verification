package timeDetection;

import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import org.junit.Before;
import org.junit.Test;
import slicing.Slice;
import slicing.TimeStatements;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by giovanni on 06/07/2017.
 */
public class TestTimeSemantic {

    ASTClass c;

    @Before
    public void setUp() throws Exception {
        String dir = load("config/methods.csv");
        dir = dir.substring(0, dir.lastIndexOf("/"));
        TemporalInfo.getInstance().loadUserDefined(dir);
        String f = load("kafka_bug/WorkerCoordinator_issue.java");
        List<ASTClass> cs = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/")));
        c = cs.get(0);
    }

    @Test
    public void RunIt() throws Exception {
        Slice.slice(c);
    }

    @Test
    public void TestListStms() throws Exception {
        Slice.slice(c);
        List<IASTStm> l = TimeStatements.getInstance().getStms();
        assertTrue("There should be some statements", l.size()>0);
        assertEquals(l.size(), 9);
        int[] lines = {50,51,53,56,61,64,68,69,70};
        for(int i = 0; i < 7; i++){
            assertEquals(lines[i], l.get(i).getLine());
        }

    }



    private String load(String s) {
        return TestTimeSemantic.class.getClassLoader().getResource(s).getFile();
    }
}
