package instrumentation;

import java.lang.instrument.Instrumentation;

/**
 * Created by giovanni on 27/04/2017.
 */
public class TestAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.err.println("AGENT INJECTED");
        if(agentArgs != null){
            System.out.println("Args: " + agentArgs + "\n");
            String confPath = agentArgs;
            ReadConfFile readConfFile = new ReadConfFile(confPath);
            readConfFile.start();
        }

        // registers the transformer
        inst.addTransformer(new TestInstrumentation());
    }
}
