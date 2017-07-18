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
public class ModelAndValue {
    public static void main(String[] args) {
        String name = args[0];
        String root_path = args[1];
        List<ASTClass> result = JDTVisitor.parse(name, root_path);
        for(ASTClass c : result){
            ClassAnalyzer ca = new ClassAnalyzer(c);
            ca.setGetModel(true);
            HashMap<IASTMethod, List<VariableNotCorrect>> err = ca.getErrors();
            for(IASTMethod m : err.keySet()) {
                if(m.getName().equals(args[2])) {
                    List<VariableNotCorrect> vars = err.get(m);
                    for (VariableNotCorrect v : vars) {
                        if(!v.getVarName().equals(args[3]))
                            continue;
                        System.out.println(v.getMinModel());
                        System.out.println(v.getMaxModel());
                        System.out.println("________");
                    }
                }
            }

        }
    }
}
