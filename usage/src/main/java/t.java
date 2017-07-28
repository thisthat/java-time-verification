import PCFG.creation.IM2CFG;
import PCFG.optimization.OptimizeTimeAutomata;
import PCFG.structure.PCFG;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.ParseCSV;
import intermediateModelHelper.indexing.IndexingProject;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by giovanni on 02/05/2017.
 */


public class t {

    private static String name(String inputString){
        boolean first = false;
        int pos = 0;
        for (int i = 1; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            char cc = inputString.charAt(i-1);
            if(cc == '/' && Character.isUpperCase(c) && !first){
                first = true;
            }
            if(c == '/' && first){
                pos = i;
            }
        }
        if(pos == 0) return inputString;
        return inputString.substring(0, pos);
    }

    public static void main(String[] args) throws Exception {


        if (args.length != 1) {
            System.err.println("Usage: filePath className methodName modelName constraintName envPath");
            return;
        }

        class struct {
            String c;
            String m;
            String s;

            public struct(String c, String m, String s) {
                this.c = c;
                this.m = m;
                this.s = s;
            }

            public String getC() {
                return c;
            }

            public String getM() {
                return m;
            }

            public String getS() {
                return s;
            }
        }

        String file = args[0];
        List<struct> entries = new ArrayList<>();
        ParseCSV parseCSV = new ParseCSV(file) {

            @Override
            protected void handleHeader(String[] header) {

            }

            @Override
            protected void handleRow(String className, String methodName, String[] signature) {
                struct s = new struct(className, methodName, signature[0]);
                //System.out.println(a);
                if(!entries.contains(s))
                    entries.add(s);
            }

        };
        parseCSV.setSplitSignature(false);
        parseCSV.start();

        long start = System.currentTimeMillis();
        File outputDir = new File("outputDir/");
        List<ASTClass> c = new ArrayList<>();
        List<IASTMethod> mm = new ArrayList<>();
        Iterator<File> i = IndexingProject.getJavaFiles(outputDir.getAbsolutePath());
        while(i.hasNext()){
            File ff = i.next();
            String f = ff.getAbsolutePath();
            c.addAll(JDTVisitor.parse(f));
        }

        for(ASTClass zlass : c){
            for(struct zz : entries){
                if(zz.c.toLowerCase().equals(zlass.fullName().toLowerCase())){
                    for(IASTMethod m : zlass.getMethods()){
                        if(m.getName().toLowerCase().equals(zz.m.toLowerCase())){
                            if(m.getParameters().toString().toLowerCase().equals(zz.s.toLowerCase())){
                                mm.add(m);
                                IM2CFG p = new IM2CFG();
                                p.addClass(zlass,m);
                                PCFG graph = p.buildPCFG();
                                graph.optimize();
                                graph.optimize(new OptimizeTimeAutomata());
                            }
                        }
                    }
                }
            }
        }
        long ms = System.currentTimeMillis() - start;
        System.out.println("Took: " + ms);

    }

    public static void main_2(String[] args) throws Exception {


        if (args.length != 1) {
            System.err.println("Usage: filePath className methodName modelName constraintName envPath");
            return;
        }

        class struct {
            String c;
            String m;
            String s;

            public struct(String c, String m, String s) {
                this.c = c;
                this.m = m;
                this.s = s;
            }

            public String getC() {
                return c;
            }

            public String getM() {
                return m;
            }

            public String getS() {
                return s;
            }
        }

        String file = args[0];
        List<struct> entries = new ArrayList<>();
        ParseCSV parseCSV = new ParseCSV(file) {

            @Override
            protected void handleHeader(String[] header) {

            }

            @Override
            protected void handleRow(String className, String methodName, String[] signature) {
                struct s = new struct(className, methodName, signature[0]);
                //System.out.println(a);
                if(!entries.contains(s))
                    entries.add(s);
            }

        };
        parseCSV.setSplitSignature(false);
        parseCSV.start();
        System.out.println(entries.size());

        File outputDir = new File("outputDir/");
        List<ASTClass> c = new ArrayList<>();
        List<IASTMethod> mm = new ArrayList<>();
        Iterator<File> i = IndexingProject.getJavaFiles(outputDir.getAbsolutePath());
        while(i.hasNext()){
            File ff = i.next();
            String f = ff.getAbsolutePath();
            c.addAll(JDTVisitor.parse(f));
        }
        System.out.println(c.size());

        for(ASTClass zlass : c){
            for(struct zz : entries){
                if(zz.c.toLowerCase().equals(zlass.fullName().toLowerCase())){
                    for(IASTMethod m : zlass.getMethods()){
                        if(m.getName().toLowerCase().equals(zz.m.toLowerCase())){
                            if(m.getParameters().toString().toLowerCase().equals(zz.s.toLowerCase())){
                                mm.add(m);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(mm.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter("outputDir/stats.csv"));
        writer.write("loc;stm;name\n");
        for(IASTMethod m : mm){
            int loc = 0;
            String code = filterString(m.getCode());
            String[] e = code.split("\n");
            for(String s : e){
                if(!s.trim().equals(""))
                    loc++;
            }

            final int[] stm = {0};
            m.visit(new DefaultASTVisitor(){
                @Override
                public void enterSTM(IASTStm s) {
                    stm[0]++;
                }
            });

            writer.write(String.format("%d;%d;%s\n", loc, stm[0], m.getName()));
            writer.flush();
        }
        writer.close();

    }

    private static String filterString(String code) {
        String partialFiltered = code.replaceAll("/\\*.*\\*/", "");
        String fullFiltered = partialFiltered.replaceAll("//.*(?=\\n)", "");
        fullFiltered = fullFiltered.substring( fullFiltered.indexOf('{') + 1);
        fullFiltered = fullFiltered.substring( 0, fullFiltered.lastIndexOf('}'));
        return fullFiltered;
    }

    public static void main_1(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: filePath className methodName modelName constraintName envPath");
            return;
        }

        String file = args[0];
        List<String> entries = new ArrayList<>();
        ParseCSV parseCSV = new ParseCSV(file) {
            @Override
            protected void handleHeader(String[] header) {

            }

            @Override
            protected void handleRow(String className, String methodName, String[] signature) {
                String a = methodName.replace(".", "/");
                //System.out.println(a);
                a = name(a) + ".java";
                //System.out.println(a);
                if(!entries.contains(a))
                    entries.add(a);
            }

        };
        parseCSV.start();

        //System.out.println(entries.size());
        //System.exit(0);

        boolean found = false;
        File outputDir = new File("outputDir/");
        outputDir.mkdirs();
        List<String> search = new ArrayList<>();
        {
            //Iterator<File> i = IndexingProject.getJavaFiles("/Users/giovanni/repository/ddd");
            Iterator<File> i = IndexingProject.getJavaFiles("/Users/giovanni/Documents/research/correct_behavior/repositories");
            while (i.hasNext() && !found) {
                File ff = i.next();
                String filename = ff.getAbsolutePath();
                search.add(filename);
            }
        }
        {
            Iterator<File> i = IndexingProject.getJavaFiles("/Users/giovanni/repository/ddd");
            //Iterator<File> i = IndexingProject.getJavaFiles("/Users/giovanni/Documents/research/correct_behavior/repositories");
            while (i.hasNext() && !found) {
                File ff = i.next();
                String filename = ff.getAbsolutePath();
                search.add(filename);
            }
        }

        /*
        for(String f : entries){
            for(int i = 0; i < search.size() && !found; i++){
                String s = search.get(i);
                if(s.contains(f)){
                    String n = f.substring(f.lastIndexOf("/"));
                    String command = String.format("cp %s %s", s, outputDir.getAbsolutePath() + n);
                    //System.out.println(command);
                    Process p = Runtime.getRuntime().exec(command);
                    p.waitFor();
                }
            }
            //System.out.println("next file");
        }
        */

        List<String> c = new ArrayList<>();
        Iterator<File> i = IndexingProject.getJavaFiles(outputDir.getAbsolutePath());
        while(i.hasNext()){
            File ff = i.next();
            String f = ff.getAbsolutePath();
            String n = f.substring(f.lastIndexOf("/"));
            c.add(n);
        }

        System.out.println(c.size());
        System.out.println(entries.size());

        for(String s : entries){
            String n = s.substring(s.lastIndexOf("/"));
            //System.out.println(n);
            found = false;
            for(String z : c){
                String nn = z.substring(z.lastIndexOf("/"));
                //System.out.println(nn);
                if(nn.toLowerCase().equals(n.toLowerCase())){
                    found = true;
                }
            }
            if(!found){
                System.out.println("Missing: " + n);
            }
        }
    }
}
