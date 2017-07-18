package smt;

import converter.ClassAnalyzer;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import smt.exception.VariableNotCorrect;

import java.util.HashMap;
import java.util.List;

/**
 * Created by giovanni on 18/07/2017.
 */
public class SingleFile {
    public static void main(String[] args) {
        String file = args[0];
        String root = args[1];
        List<ASTClass> result = JDTVisitor.parse(file, root);
        for(ASTClass c : result){
            ClassAnalyzer ca = new ClassAnalyzer(c);
            try {
                HashMap<IASTMethod, List<VariableNotCorrect>> err = ca.getErrors();
            }catch (Exception x) {
                System.out.println("In file " + c.getPath());
                throw x;
            }
        }
    }
}
