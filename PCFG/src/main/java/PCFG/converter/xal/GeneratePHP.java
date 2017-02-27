package PCFG.converter.xal;

import IntermediateModel.structure.ASTClass;
import PCFG.structure.CFG;
import PCFG.structure.PCFG;
import PCFG.structure.node.Node;

/**
 * Created by giovanni on 16/02/2017.
 */

public class GeneratePHP extends GenerateCode {

    public GeneratePHP(PCFG pcfg, ASTClass c){
        super(pcfg,c, c.getPackageName().replace(".","_") + "_" + c.getName() + ".php");
    }

    public String getFileName() {
        return fileName;
    }

    public String getPHPClass(){
        StringBuilder out = new StringBuilder();
        out.append("<?php\n");
        for(CFG c : pcfg.getCFG()) {
            out.append(getPHPClass(c));
        }
        out.append("?>\n");
        return out.toString();
    }

    private String getPHPClass(CFG c) {
        StringBuilder out = new StringBuilder();
        out.append("class " + sanitize(c.getName() + c.getID()) + " {\n");
        for(Node n : c.getV()){
            out.append(getPHPMethod(n));
        }
        out.append("}\n");
        return out.toString();
    }

    private String getPHPMethod(Node n) {
        StringBuilder out = new StringBuilder();
        out.append("\tpublic function " + sanitize(n.getName()) + "() {\n");
        out.append("/*\n");
        out.append(n.getCode());
        out.append("\n*/\n");
        out.append("\t}\n");
        return out.toString();
    }

    @Override
    String generateString() {
        return getPHPClass();
    }

}
