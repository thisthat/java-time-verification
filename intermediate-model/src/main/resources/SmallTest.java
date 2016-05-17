import java.util.ArrayList;
import java.util.Collection;

public class Test extends Object implements IFloodlightModule, INetTopologyService, IOFMessageListener {
	@Deprecated
	private Test(int i, int k) {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();


		for (int j = 0; j < 10; j++) {
			j = i << 1 + (40 * 48 / 10);
			l.add(INetTopologyService.class);
		}

		for(int j : listOfElms){
			j = i >> 1;
		}

		if(i > 0){
			Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
			j = 1+2;
		}
		else
			j = 3+2;


	}
}