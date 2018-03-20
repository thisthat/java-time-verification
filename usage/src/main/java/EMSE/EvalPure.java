package EMSE;

import daikon.invariant.Reader;
import daikon.parser.structure.MethodInvariants;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ExtractTimeAttribute;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.ParseCSV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EvalPure {

    static String pathInv = "./projects/";
    static String pathPrj = "./invariants/";
    static Statistics stats;
    static {
       stats = new Statistics();
    }

    static class Statistics {
        int yPure = 0;
        int nPure = 0;
        List<String> yPureList = new ArrayList<>();
        List<String> nPureList = new ArrayList<>();

        public void addYesPure(String method, String pars){
            yPure++;
            String ins = method + pars;
            if(yPureList.contains(ins))
                return;
            yPureList.add(ins);
        }
        public void addNoPure(String method, String pars){
            nPure++;
            String ins = method + pars;
            if(nPureList.contains(ins))
                return;
            nPureList.add(ins);
        }
    }

    static class Line {
        String id;
        String projectName;
        String className;
        String methodName;
        String signature;
        String path;

        public Line(String id, String projectName, String className, String methodName, String signature, String path) {
            this.id = id;
            this.projectName = projectName;
            this.className = className;
            this.methodName = methodName;
            this.signature = signature;
            this.path = path;
        }
    }

    static class ParseCSVEval extends ParseCSV {

        List<Line> out = new ArrayList<>();

        public ParseCSVEval(String path) {
            super(path);
        }

        @Override
        protected void parseRow(String[] row) {
            out.add(new Line(
                    row[0],
                    row[1],
                    row[2],
                    row[3],
                    row[4],
                    row[5]
            ));
        }

        @Override
        protected void handleHeader(String[] header) {

        }

        @Override
        protected void handleRow(String className, String methodName, String[] signature) {

        }
    }


    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println("Usage with the following path: [csv]");
            System.exit(0);
        }
        //load types for each project

        String file = args[0];
        ParseCSVEval p = new ParseCSVEval(file);
        List<Line> lines = p.out;
        //for each line
        for(Line l : lines) {
            handle_single(l.id, l.projectName, l.className, l.methodName, l.signature, l.path);
        }


    }

    public static void handle_single(String id, String projectName, String className, String methodName, String signature, String path){
        String invFile = pathInv + projectName + "/" + className + ".txt";
        String prjPath = pathPrj + projectName;
        File finv = new File(invFile);
        if(!finv.exists()){
            try {
                boolean b = finv.createNewFile();
                if(!b){
                    System.err.println("Cannot create:" + invFile);
                }
            } catch (IOException e) {
                System.err.println("Cannot create:" + invFile);
            }
        }
        // we have the inv file for sure at this point
        List<MethodInvariants> invs;
        try {
            invs = Reader.readInvariant(invFile);
        } catch (IOException e) {
            System.err.println("Cannot parse file: " + invFile);
            return;
        }
        List<ASTClass> classes = JDTVisitor.parse(path, prjPath);
        ASTClass targetClass = null;
        for(ASTClass c : classes){
            if(c.fullName().equals(className)){
                targetClass = c;
            }
        }
        if(targetClass == null){
            System.err.println("Cannot find class " + className);
            System.err.println("in file " + path);
        }
        // we are sure to have the right class here in memory
        String methodToCheck = targetClass.fullName() + "." + methodName;
        String[] pars = signature.trim().substring(1, signature.length() -1).split(",");
        // we have prepared the vars

        List<String> timeVars = new ArrayList<>();
        ExtractTimeAttribute timeAttribute = new ExtractTimeAttribute(targetClass);
        for(IASTVar p : timeAttribute.getTimeAttributes()){
            timeVars.add(p.getName());
        }
        // we have collected the timed attributes now
        boolean isPure = true;
        for(MethodInvariants i : invs){
            if(i.isMethod(methodToCheck, pars) && i.isExit()){
                isPure &= i.isPure(timeVars);
            }
        }
        if(isPure)
            stats.addYesPure(methodToCheck, signature);
        else
            stats.addNoPure(methodToCheck, signature);
    }
}

