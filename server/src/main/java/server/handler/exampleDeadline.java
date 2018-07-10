package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.*;
import intermediateModel.structure.expression.ASTIdentifier;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;
import intermediateModelHelper.heuristic.definition.AssignmentTimeVar;
import intermediateModelHelper.heuristic.definition.TimeoutResources;
import server.handler.middleware.ParsePars;
import server.helper.Answer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class exampleDeadline implements HttpHandler {

    private List<ASTClass> classes;
    private String file_path;
    private ASTClass zclass;

    public exampleDeadline() {
        file_path = getClass().getClassLoader().getResource("deadline.java").getFile();
        classes = JDTVisitor.parse(file_path, file_path.substring(0, file_path.lastIndexOf("/")));
        zclass = classes.get(0);
        int start = 113;
        int end = 232;
        ASTRE timeout = new ASTRE(122, 129, new ASTIdentifier(122, 129, "timeout"));
        final ASTWhile[] exp_deadline = new ASTWhile[1];
        zclass.visit(new DefaultASTVisitor(){
            @Override
            public void enterASTWhile(ASTWhile elm) {
                exp_deadline[0] = elm;
            }
        });
        ASTDeadline d = new ASTDeadline(start, end, timeout);
        d.addStms(exp_deadline[0]);
        List<IASTStm> stms = new ArrayList<>();
        stms.add(d);
        zclass.visit(new DefaultASTVisitor(){
            @Override
            public void enterASTTry(ASTTry elm) {
                elm.getTryBranch().setStms(stms);
            }
        });
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        for(ASTClass c : classes){
            getFile.AnnotateEnv a = new getFile.AnnotateEnv();
            a.start(c);
            ApplyHeuristics ah = new ApplyHeuristics();
            //ah.subscribe(AnnotatedTypes.class);
            ah.subscribe(TimeoutResources.class);
            ah.subscribe(AssignmentTimeVar.class);

            ah.analyze(c);
            //annotate each method
            //for(IASTMethod m : c.getMethods()){
            //    m.setDeclaredVars(new Env());
            //}
            for(Constraint cnst : ah.getTimeConstraint()){
                cnst.removeElm();
            }
            c.setPath(file_path);
        }
        //annotate with Time


        // send response
        ObjectMapper json = ParsePars.getOutputFormat(new HashMap<>());
        json.enable(SerializationFeature.INDENT_OUTPUT);

        String response = "";
        try {
            response = json.writeValueAsString(classes);
        } catch (Exception e){
            //LOGGER.catching(e);
        }
        //LOGGER.debug(response);
        Answer.SendMessage(response, he);
    }
}
