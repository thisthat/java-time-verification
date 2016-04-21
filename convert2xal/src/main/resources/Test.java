package expected;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class Test {

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        topology = context.getServiceImpl(ILinkDiscoveryService.class);
        GenerateTopologyAsync myRunnable = new GenerateTopologyAsync(this);
        createTopologyThread = new Thread(myRunnable);
        createTopologyThread.start();
        mongodb.connect();
        predictionProvider = new PredictionHandler(mongodb);
        behaviourProvider = new BehaviourManager(mongodb, predictionProvider);
    }

}
