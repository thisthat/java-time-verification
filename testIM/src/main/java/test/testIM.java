package test;

import java.io.File;
import java.util.List;

import IntermediateModel.structure.*;
import IntermediateModel.visitors.creation.JDTVisitor;


/**
 * Created by giovanni on 14/02/2017.
 */
public class testIM {

    public static void main(String[] args) {
        File oldTmpFile = new File(args[0]);
        File newTmpFile = new File(args[1]);
        List<ASTClass> c = JDTVisitor.parse(oldTmpFile);
        List<ASTClass> c1 = JDTVisitor.parse(newTmpFile);

    }
}
