package uppaal.replaceParameter;

import org.javatuples.Pair;
import uppaal.NTA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by giovanni on 03/05/2017.
 */
public class HandleReplacement {

    public static void createModelsFromTraces(String model, String traces, String dir){
        ReplaceTraces replacer = new ReplaceTraces(model);
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
        // add data
        for(TraceItem t : instances){
            int thId = t.getThreadID();
            List<Pair<String,String>> vals = groupById.get(thId);
            vals.add(new Pair<>(t.getVarName(), t.getVarValue()));
        }
        // create an instance of the model per thread
        for(Integer thid : threadIds){
            List<List<Pair<String,String>>> versions = splitVersion(groupById.get(thid));
            int vId = 0;
            for(List<Pair<String,String>> v : versions){
                vId++;
                NTA thModel = replacer.replace(v);
                //writer = new BufferedWriter(new FileWriter("graph.xal"));
                try {
                    thModel.writeXMLWithPrettyLayout(dir + "thread_" + thid + "_v" + vId + ".xml");
                } catch (Exception e) {
                    System.err.println("Cannot create file for thread #" + thid + "_v"+ vId);
                }
            }

        }
    }

    public static List<List<Pair<String,String>>> splitVersion(List<Pair<String,String>> trace){
        List<List<Pair<String,String>>> out = new ArrayList<>();
        List<Pair<String,String>> tmp = new ArrayList<>();
        for(Pair<String,String> elm : trace){
            //is a value already there?
            boolean f = false;
            String varName = elm.getValue0();
            for(Pair<String,String> visited : tmp){
                if(varName.equals(visited.getValue0())){
                    f = true;
                }
            }
            //we come across the same var? -> split
            if(f){
                out.add(tmp);
                tmp = new ArrayList<>();
            }
            tmp.add(elm);
        }
        out.add(tmp);
        return out;
    }

}
