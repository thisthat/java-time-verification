package daikon.instrumentation.preprocess;

import java.util.HashSet;
import java.util.Set;

public class WatchPoints {

    //Singleton
    private static WatchPoints instance = null;
    private WatchPoints(){}
    public static synchronized WatchPoints getInstance() {
        if(instance == null){
            instance = new WatchPoints();
        }
        return instance;
    }

    Set<WatchPoint> points = new HashSet<>();

    public void addWatchingPoint(String className, String methodName, int line, Set<String> variableName){
        points.add(new WatchPoint(className, methodName, line, variableName));
    }

    public void addWatchingPoint(String className, String methodName, int line, String[] variableName){
        Set<String> vars = new HashSet<>();
        for(String v : variableName){
            vars.add(v);
        }
        points.add(new WatchPoint(className, methodName, line, vars));
    }

    public int size(){
        return points.size();
    }

    public boolean shouldWatch(String className) {
        for(WatchPoint wp : points){
            if(wp.className.equals(className))
                return true;
        }
        return false;
    }

    public Set<String> getMethodDefinitions(String className){
        Set<String> out = new HashSet<>();
        for(WatchPoint p : points){
            if(p.isTheOne(className))
                out.add(p.printAsMethodDef());
        }
        return out;
    }

    public Set<WatchPoint> getWatchPoints(String className){
        Set<WatchPoint> out = new HashSet<>();
        for(WatchPoint p : points){
            if(p.isTheOne(className))
                out.add(p);
        }
        return out;
    }

}