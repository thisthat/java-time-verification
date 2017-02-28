package PCFG.converter.xal;

import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.ASTClass;
import IntermediateModel.structure.ASTImport;
import PCFG.structure.CFG;
import PCFG.structure.PCFG;
import PCFG.structure.node.Node;

/**
 * Created by giovanni on 27/02/2017.
 */
public class GenerateJava extends GenerateCode {


    public GenerateJava(PCFG pcfg, ASTClass c) {
        super(pcfg, c, c.getPackageName().replace(".","_") + "_" + c.getName() + ".java");
    }

    public String getJavaClass(){
        StringBuilder out = new StringBuilder();
        out.append(this.c.getPackageName());
        out.append("\n\n");
        for(ASTImport imp : this.c.getImports()){
            out.append(imp.toString());
        }
        out.append(String.format("%s class %s {\n", String.valueOf(c.getAccessRight()), c.getName()));
        for(CFG c : pcfg.getCFG()) {
            out.append(getJavaClass(c));
        }
        out.append("\n//Java Code\n\n");
        for(IASTMethod m : this.c.getMethods()){
            out.append("\t");
            out.append(m.getCode());
            out.append("\n");
        }
        out.append("}");
        return out.toString();
    }

    private String getJavaClass(CFG c) {
        StringBuilder out = new StringBuilder();
        for(Node n : c.getV()){
            out.append(getJavaMethod(n));
        }
        return out.toString();
    }

    private String getJavaMethod(Node n) {
        StringBuilder out = new StringBuilder();
        out.append(String.format("\tpublic void %s () {\n", n.getName()));
        out.append("\t\t" + n.getCode().replace("\n","\t\n"));
        out.append("\n\t}\n");
        return out.toString();

    }

    private IASTMethod getMethod(String name){
        for(IASTMethod m : this.c.getMethods()){
            if(m.getName().equals(name))
                return m;
        }
        return null;
    }

    @Override
    String generateString() {
        return getJavaClass();
    }
}
