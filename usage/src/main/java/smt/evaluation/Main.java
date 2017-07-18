package smt.evaluation;

import converter.ClassAnalyzer;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;
import smt.exception.VariableNotCorrect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Main {

    public static long timeSpent, timeSpentWriting, start, end;

    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.out.println("Use with the project path and project name as argument");
            System.exit(0);
        }
        timeSpent = 0;
        timeSpentWriting = 0;
        //get root path
        String name = args[0];
        String root_path = args[1];
        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(name + ".csv"));
        writer.write("class;method;line;variable\n");

        //stats
        int nClass = 0;
        int nFiles = 0;
        int nMethod = 0;
        int nMethodError = 0;
        int nClassError = 0;
        int nError = 0;

        boolean atLeastOneError;

        List<String> errors = new ArrayList<>();

        long init = System.currentTimeMillis();
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            nFiles++;
            //each class
            List<ASTClass> result = JDTVisitor.parse(filename, root_path);
            for(ASTClass c : result){
                nClass++;
                nMethod = nMethod + c.getMethods().size();
                ClassAnalyzer ca = new ClassAnalyzer(c);
                HashMap<IASTMethod, List<VariableNotCorrect>> err = new HashMap<>();
                try {
                     err = ca.getErrors();
                } catch (Exception e){
                    System.out.println("Error in class " + c.fullName());
                    System.out.println(c.getPath());
                    throw e;
                }
                start();
                atLeastOneError = false;
                for(IASTMethod m : err.keySet()) {
                    List<VariableNotCorrect> vars = err.get(m);
                    if(vars.size() > 0) {
                        nMethodError++;
                        atLeastOneError = true;
                    }
                    for(VariableNotCorrect v : vars) {
                        nError++;
                        String e = String.format(
                                "%s;%s;%d;%s\n",
                                c.fullName(), m.getName(), v.getWhere().getLine(), v.getVarName()
                        );
                        if(!errors.contains(e)) {
                            errors.add(e);
                            writer.write(e);
                            writer.flush();
                        }
                    }
                }
                if(atLeastOneError){
                    nClassError++;
                }
                stop();
            }
        }
        long ending = System.currentTimeMillis() - init ;
        writer.close();
        timeSpent = ending - timeSpentWriting;
        System.out.println("Total Writing: " + timeSpentWriting);
        System.out.println("Total Net: " + timeSpent);
        System.out.println("Total Time: " + ending);
        BufferedWriter stats = new BufferedWriter(new FileWriter(name + ".log"));
        stats.write("\nTime Writing: " + timeSpentWriting + "ms");
        stats.write("\nTime Net: " + timeSpent + "ms");
        stats.write("\nTotal Time: " + ending + "ms");
        stats.write("\n# Files: " + nFiles);
        stats.write("\n# Classes: " + nClass);
        stats.write("\n# Methods: " + nMethod);
        stats.write("\n# Error: " + nError);
        stats.write("\n# Classes w/ error: " + nClassError);
        stats.write("\n# Methods w/ error: " + nMethodError);
        stats.flush();
        stats.close();
    }

    public static void start(){
        start = System.currentTimeMillis();
    }


    public static void stop(){
        end = System.currentTimeMillis();
        timeSpentWriting += (end-start);
    }
}
