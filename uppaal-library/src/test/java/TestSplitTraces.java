import org.junit.Test;
import uppaal.replaceParameter.HandleReplacement;
import uppaal.replaceParameter.ReadTraces;
import uppaal.replaceParameter.TraceItem;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by giovanni on 04/05/2017.
 */
public class TestSplitTraces {

    @Test
    public void TestSplit() {
        String traces = TestSplitTraces.class.getClassLoader().getResource("traces.txt").getFile();
        List<TraceItem> instances = ReadTraces.getTraces(traces);
        //info on thread ids
        List<Integer> threadIds = HandleReplacement.getThreads(instances);
        //Group by Thread ID the substitutions
        HashMap<Integer,List<TraceItem>> groupById = HandleReplacement.groupById(instances, threadIds);
        assertEquals(7, threadIds.size());
        int th = 287;
        List<TraceItem> trace = groupById.get(th);
        List<List<TraceItem>> out = HandleReplacement.splitVersion(trace);
        assertEquals(9, trace.size());
        assertEquals(7, out.size());
    }



}


