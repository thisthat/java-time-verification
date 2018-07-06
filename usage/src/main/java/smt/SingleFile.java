package smt;

import converter.ClassAnalyzer;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import smt.exception.VariableNotCorrect;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by giovanni on 18/07/2017.
 */
public class SingleFile {
    public static void main(String[] args) {
        //ModelCreator._debug_ = true;
        String file = args[0];
        String root = args[1];
        String name = args[2];
        System.out.println(file);
        File loadFromHD = new File("config/" + name + "_types.csv");
        if(loadFromHD.exists()){
            TemporalInfo.getInstance().loadUserTypes(loadFromHD.getAbsolutePath());
        }
        List<ASTClass> result = JDTVisitor.parse(file, root);
        for(ASTClass c : result){
            ClassAnalyzer ca = new ClassAnalyzer(c);
            ca.setGetModel(true);
            try {
                HashMap<IASTMethod, List<VariableNotCorrect>> err = ca.getErrors();
                for(IASTMethod m : err.keySet()){
                    for (VariableNotCorrect v : err.get(m)) {
                        System.out.println(m.getName());
                        System.out.println(v.getVarName() + "@" + v.getWhere().getLine());
                        System.out.println("Min");
                        System.out.println(v.getMinModel());
                    }
                }

            } catch (Exception x) {
                System.out.println("In file " + c.getPath());
                throw x;
            }
        }
    }
}
