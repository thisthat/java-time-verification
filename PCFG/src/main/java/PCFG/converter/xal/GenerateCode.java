package PCFG.converter.xal;

import IntermediateModel.structure.ASTClass;
import PCFG.converter.Settings;
import PCFG.structure.PCFG;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by giovanni on 27/02/2017.
 */
public abstract class GenerateCode {

    protected PCFG pcfg;
    protected ASTClass c;
    protected String fileName;

    public static String sanitize(String n){
        n = n.replace("::", "_");
        n = n.replace(".", "_");
        return n;
    }

    public GenerateCode(PCFG pcfg, ASTClass c, String fileName){
        this.pcfg = pcfg;
        this.c = c;
        this.fileName = fileName;
    }

    abstract String generateString();

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
            writer.write(generateString());
            writer.close();
        } catch (Exception e) {
            //not create the class is the best option
        }
    }
}
