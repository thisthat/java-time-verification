package test.library.juppaal;


import com.uppaal.engine.Engine;
import com.uppaal.engine.EngineException;
import com.uppaal.engine.Problem;
import com.uppaal.engine.QueryFeedback;
import com.uppaal.model.core2.Document;
import com.uppaal.model.core2.PrototypeDocument;
import com.uppaal.model.system.Transition;
import com.uppaal.model.system.UppaalSystem;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by giovanni on 19/04/2017.
 */
public class TestUppaal {
    public static void main(String[] args) throws IOException, EngineException {
        Engine e = new Engine();
        e.setServerPath("/Users/giovanni/repository/java-xal/testJUppaal/lib/MacOS/server");
        e.connect();
        Document doc = new PrototypeDocument().load(new URL("file:///Users/giovanni/repository/java-xal/graph.xml"));
        Vector<Problem> problems = new Vector<Problem>();
        UppaalSystem system = e.getSystem(doc, problems);
        QueryFeedback feed = new QueryFeedback() {
            @Override
            public void setLength(int i) {

            }

            @Override
            public void setCurrent(int i) {

            }

            @Override
            public void setTrace(char c, Vector<Transition> vector, int i) {

            }
        };
        char ans = e.query(system, "", "E<> not deadlock", feed);
        switch (ans){
            case 'T' :
                System.out.println("OK");
                break;
            case 'F' :
                System.out.println("Not Valid");
                break;
            default:
                System.out.println("Error");
        }

    }
}
