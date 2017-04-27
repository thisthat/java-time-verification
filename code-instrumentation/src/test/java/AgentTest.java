import static org.junit.Assert.assertEquals;

import instrumentation.Testing;
import org.junit.Test;

public class AgentTest {

    @Test
    public void shouldInstantiateSleepingInstance() throws InterruptedException {

        Testing sleeping = new Testing();
        sleeping.randomSleep();
    }

}