package daikon.annotation;

import intermediateModel.interfaces.IASTVar;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WatchingPoints {
    private class Point {
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
            sb.append(String.format("%s;%s;%d;%s\n", className, methodName, line, Arrays.toString(vars.toArray())));
            return sb.toString();
        }
    }

    Set<Point> variables = new HashSet<>();

    public void addWatchingPoint(String className, String methodName, int line, Set<IASTVar> variableName){
        variables.add(new Point(className, methodName, line, variableName));
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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for(Point p : variables)
            sb.append(p.toString());
        return sb.toString();
    }

    public void toFile(String filename){
        try(  BufferedWriter writer = new BufferedWriter(new FileWriter( filename))){
            writer.write(this.toString());
        }  catch (IOException e) {
            System.err.printf("Couldn't write to file [%s]\n", filename);
        }
    }
}
