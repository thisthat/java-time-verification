package instrumentation;

import java.lang.instrument.Instrumentation;

/**
 * Created by giovanni on 27/04/2017.
 */
public class TestAgent {
    public static void premain(String agentArgs, Instrumentation inst) {

        // registers the transformer
        inst.addTransformer(new TestInstrumentation());
    }
}
