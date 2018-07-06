package server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.interfaces.IASTStm;
import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTDeadline;
import intermediateModel.structure.ASTRE;
import intermediateModel.structure.ASTTry;
import intermediateModel.structure.expression.ASTIdentifier;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModelHelper.envirorment.temporal.structure.Constraint;
import intermediateModelHelper.heuristic.definition.AssignmentTimeVar;
import intermediateModelHelper.heuristic.definition.TimeoutResources;
import server.handler.middleware.ParsePars;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class exampleTestNeighbours implements HttpHandler {

    private List<ASTClass> classes;
    private String file_path;
    private ASTClass zclass;

    public exampleTestNeighbours() {
        file_path = exampleTestNeighbours.class.getClassLoader().getResource("TestNeighbors.java").getFile();
        classes = JDTVisitor.parse(file_path, file_path.substring(0, file_path.lastIndexOf("/")));
        zclass = classes.get(0);

        ASTRE timeout = new ASTRE(187, 194, new ASTIdentifier(187, 194, "timeout"));
        ASTDeadline d = new ASTDeadline(178, 341, timeout);

        zclass.visit(new DefaultASTVisitor(){
            @Override
            public void enterASTTry(ASTTry elm) {
                d.setStms(elm.getTryBranch().getStms());
            }
        });

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
            for(IASTMethod m : c.getMethods()){
                m.setDeclaredVars();
            }
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
        he.getResponseHeaders().add("Content-Type","application/json");
        he.sendResponseHeaders(200, response.length());
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
