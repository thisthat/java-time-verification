package PCFG.converter.xal;

import IntermediateModel.structure.ASTClass;
import PCFG.converter.Settings;
import PCFG.structure.CFG;
import PCFG.structure.PCFG;
import PCFG.structure.node.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by giovanni on 16/02/2017.
 */

public class GeneratePHP {

    PCFG pcfg;
    ASTClass c;
    String fileName;

    public static String sanitize(String n){
        n = n.replace("::", "_");
        n = n.replace(".", "_");
        return n;
    }

    public GeneratePHP(PCFG pcfg, ASTClass c){
        this.pcfg = pcfg;
        this.c = c;
        this.fileName = c.getPackageName().replace(".","_") + "_" + c.getName() + ".php";
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

    public void generateClass(){
        try{
            File outDir = new File(Settings.getInstance().getOutputDir());
            if(!outDir.exists()){
                outDir.mkdirs();
                System.out.println("[DEBUG] Output directory created >> " + outDir.getAbsolutePath());
            }
            String file_name = outDir.getAbsolutePath() + "/" + fileName;
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(file_name));
            writer.write(getPHPClass());
            writer.close();
        } catch (Exception e) {
            //not create the class is the best option
        }
    }

}
