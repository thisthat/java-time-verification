package smt.evaluation;

import converter.ClassAnalyzer;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.indexing.IndexingProject;
import smt.exception.VariableNotCorrect;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 17/07/2017.
 */
public class Main {

    public static long timeSpent, timeSpentWriting, start, end;

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Use with the project path as argument");
            System.exit(0);
        }
        timeSpent = 0;
        timeSpentWriting = 0;
        //get root path
        String root_path = args[1];
        //get all files
        Iterator<File> i = IndexingProject.getJavaFiles(root_path);

        long init = System.currentTimeMillis();
        while (i.hasNext()) {

            String filename = i.next().getAbsolutePath();
            //each class
            List<ASTClass> result = JDTVisitor.parse(filename, root_path);
            for(ASTClass c : result){
                ClassAnalyzer ca = new ClassAnalyzer(c);
                HashMap<IASTMethod,List<VariableNotCorrect>> err = ca.getErrors();
                start();
                stop();
            }
        }
        long ending = System.currentTimeMillis() - init ;
        timeSpent = ending - timeSpentWriting;
        System.out.println("Total Writing: " + timeSpentWriting);
        System.out.println("Total Net: " + timeSpent);
        System.out.println("Total Time: " + ending);
    }

    public static void start(){
        start = System.currentTimeMillis();
    }


    public static void stop(){
        end = System.currentTimeMillis();
        timeSpentWriting += (end-start);
    }
}
