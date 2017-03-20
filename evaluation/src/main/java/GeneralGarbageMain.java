import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.heuristic.definition.SetTimeout;
import intermediateModelHelper.heuristic.definition.SetTimeoutPermissive;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import parser.UnparsableException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 17/03/2017.
 */
public class GeneralGarbageMain {

    public static void main(String[] args) throws IOException, UnparsableException {
        String file = args[0];
        String line;
        String base_path = "/Users/giovanni/repository/sources/github";
        String csvFile = "manual_investigation.csv";
        boolean skipFirst = true;
        List<Pair<String,Integer>> cnstsTimeout = new ArrayList<>();

        try (
                InputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
                FileWriter writer = new FileWriter(csvFile)

        ) {
            writer.write("path;line;false_positive\n");
            writer.flush();
            while ((line = br.readLine()) != null) {
                if (skipFirst) {
                    skipFirst = false;
                    continue;
                }
                String[] fields = line.split(";");
                String path = fields[1].replace("\"", "");
                List<ASTClass> lists = JDTVisitor.parse(path, base_path);
                //List<ASTClass> lists = JDTVisitor.parse(f,"/Users/giovanni/repository/java-xal/project_eval/activemq/" );
                for (ASTClass c : lists) {
                    ApplyHeuristics ah = new ApplyHeuristics();
                    ah.set__DEBUG__(false);
                    //ah.subscribe(SetTimeoutPermissive.class);
                    ah.subscribe(SetTimeout.class);
                    ah.analyze(c);
                    List<Triplet<String, IASTStm, Class>> cnst = ah.getTimeConstraint();
                    if (cnst.size() > 0) {
                        for (Triplet<String, IASTStm, Class> t : cnst) {
                            IASTStm s = t.getValue1();
                            cnstsTimeout.add(new Pair<>(path, s.getLine()));
                        }
                    }
                }
                for (ASTClass c : lists) {
                    ApplyHeuristics ah = new ApplyHeuristics();
                    ah.set__DEBUG__(false);
                    ah.subscribe(SetTimeoutPermissive.class);
                    //ah.subscribe(SetTimeout.class);
                    ah.analyze(c);
                    List<Triplet<String, IASTStm, Class>> cnst = ah.getTimeConstraint();
                    if (cnst.size() > 0) {
                        for (Triplet<String, IASTStm, Class> t : cnst) {
                            IASTStm s = t.getValue1();
                            if(!cnstsTimeout.contains(new Pair<>(path, s.getLine()))){
                                writer.write(String.format("%s;%d;\n",path, s.getLine()));
                                writer.flush();
                            }
                        }
                    }
                }
            }
        }
    }
}
