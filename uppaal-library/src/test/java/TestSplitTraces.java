import org.javatuples.Pair;
import org.junit.Test;
import uppaal.replaceParameter.HandleReplacement;
import uppaal.replaceParameter.ReadTraces;
import uppaal.replaceParameter.TraceItem;

import java.util.ArrayList;
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
        List<Integer> threadIds = new ArrayList<>();
        for(TraceItem t : instances){
            int thId = t.getThreadID();
            if(!threadIds.contains(thId))
                threadIds.add(thId);
        }
        //Group by Thread ID the substitutions
        HashMap<Integer,List<Pair<String,String>>> groupById = new HashMap<>();
        //init with empty lists each thread
        for(Integer thid : threadIds){
            groupById.put(thid, new ArrayList<Pair<String,String>>());
        }
        for(TraceItem t : instances){
            int thId = t.getThreadID();
            List<Pair<String,String>> vals = groupById.get(thId);
            vals.add(new Pair<>(t.getVarName(), t.getVarValue()));
        }
        assertEquals(7, threadIds.size());
        int th = 287;
        List<Pair<String,String>> trace = groupById.get(th);
        List<List<Pair<String,String>>> out = HandleReplacement.splitVersion(trace);
        assertEquals(9, trace.size());
        assertEquals(3, out.size());

    }
}


