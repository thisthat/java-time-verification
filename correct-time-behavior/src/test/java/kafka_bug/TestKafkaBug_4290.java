package kafka_bug;

import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import org.junit.Before;
import org.junit.Test;
import slicing.Slice;

import java.util.List;

/**
 * Created by giovanni on 06/07/2017.
 */
public class TestKafkaBug_4290 {


    @Before
    public void setUp() throws Exception {
        String dir = load("config/methods.csv");
        dir = dir.substring(0, dir.lastIndexOf("/"));
        TemporalInfo.getInstance().loadUserDefined(dir);
    }

    @Test
    public void RunIt() throws Exception {
        String f = load("kafka_bug/WorkerCoordinator_issue.java");
        List<ASTClass> cs = JDTVisitor.parse(f, f.substring(0, f.lastIndexOf("/")));
        ASTClass c = cs.get(0);
        Slice.slice(c);
    }

    private String load(String s) {
        return TestKafkaBug_4290.class.getClassLoader().getResource(s).getFile();
    }
}
