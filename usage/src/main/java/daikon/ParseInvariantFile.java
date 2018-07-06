package daikon;

import daikon.invariant.Reader;
import daikon.parser.structure.MethodInvariants;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTVar;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ExtractTimeAttribute;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseInvariantFile {
    public static void main(String[] args) throws IOException {
        if(args.length != 4){
            System.err.println("Usage with the following four paths: [file] [project] [intermediateModel.types.csv] [invariant.txt]");
            System.exit(0);
        }
        String file = args[0];
        String prj  = args[1];
        String typ  = args[2];
        TemporalInfo.getInstance().loadUserTypes(typ);
        String invariant  = args[3];

        List<MethodInvariants> invs = Reader.readInvariant(invariant);
        List<ASTClass> classes = JDTVisitor.parse(file, prj);
        if(classes.size() == 0) return;
        ASTClass c = classes.get(0);

        List<String> methods = new ArrayList<>();
        for(IASTMethod m : c.getMethods()){
            String name = c.fullName() + "." + m.getName();
            methods.add(name);
        }

        List<String> timeVars = new ArrayList<>();
        ExtractTimeAttribute timeAttribute = new ExtractTimeAttribute(c);
        for(IASTVar p : timeAttribute.getTimeAttributes()){
            timeVars.add(p.getName());
        }

        List<String> pureMethods = new ArrayList<>();
        for(String m : methods){
            boolean isPure = true;
            for(MethodInvariants i : invs){
                if(i.isMethod(m) && i.isExit()){
                    isPure &= i.isPure(timeVars);
                }
            }
            if(isPure)
                pureMethods.add(m);
        }

        System.out.println("Variables:");
        for(String v : timeVars)
            System.out.println(v);
        System.out.println("Pure methods:");
        for(String m : pureMethods)
            System.out.println(m);
        System.out.println("[Not]Pure methods:");
        for(String m : methods)
            if(!pureMethods.contains(m))
                System.out.println(m);
    }
}
