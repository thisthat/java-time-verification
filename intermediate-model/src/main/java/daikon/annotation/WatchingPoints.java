package daikon.annotation;

import intermediateModel.interfaces.IASTVar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class WatchingPoints {
    private class Point implements Comparable {
        String className;
        String methodName;
        int line;
        Set<IASTVar> vars;

        public Point(String className, String methodName, int line, Set<IASTVar> vars) {
            this.className = className;
            this.methodName = methodName;
            this.line = line;
            this.vars = vars;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getLine() {
            return line;
        }

        public Set<IASTVar> getVars() {
            return vars;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("%s;%s;%d;%s\n", className, methodName, line, printVars()));
            return sb.toString();
        }

        private String printVars() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            IASTVar[] varsArray = vars.toArray(new IASTVar[0]);
            for(int i = 0; i < varsArray.length; i++){
                IASTVar v = varsArray[i];
                sb.append(v.getName());
                if(i != vars.size() -1){
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return this.line == point.line;
        }

        @Override
        public int compareTo(Object o) {
            if (this == o) return 0;
            if (o == null || getClass() != o.getClass()) return 0;
            Point point = (Point) o;
            return this.line - point.line;
        }
    }

    Set<Point> variables = new TreeSet<>();

    public void addWatchingPoint(String className, String methodName, int line, Set<IASTVar> variableName){
        Point p = new Point(className, methodName, line, variableName);
        if(!variables.contains(p))
            variables.add(p);
    }

    public int size(){
        return variables.size();
    }

    public Set<Integer> getLines(){
        Set<Integer> out = new HashSet<>();
        for(Point p : variables){
            out.add(p.getLine());
        }
        return out;
    }

    public Set<IASTVar> getVarsByLine(int line){
        Set<IASTVar> out = new HashSet<>();
        for(Point p : variables){
            if(p.getLine() == line)
                out.addAll(p.getVars());
        }
        return out;
    }

    public void add(WatchingPoints w){
        this.variables.addAll(w.variables);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Point[] ps = variables.toArray(new Point[0]);
        Arrays.sort(ps, Comparator.comparingInt(Point::getLine));
        for(int i = 0; i < ps.length; i++) {
            Point p = ps[i];
            sb.append(p.toString());
        }
        return sb.toString();
    }

    public void toCSV(String filename){
        try(  BufferedWriter writer = new BufferedWriter(new FileWriter( filename))){
            writer.write("Class;Method;Line;ListVars\n");
            writer.write(this.toString());
        }  catch (IOException e) {
            System.err.printf("Couldn't write to file [%s]\n", filename);
        }
    }
}
