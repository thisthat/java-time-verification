import IntermediateModel.interfaces.IASTStm;

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
		System.out.print("str");
		try {
			System.out.print("str");System.out.print("str");
		} catch(Exception e){
			throw new Exception("Exc");System.out.print("str");
		} catch(IOException e){
			throw new Exception("IO Exc");System.out.print("str");
		} finally {
			System.err.print("Eccezione lanciata");System.out.print("str");
			System.err.print("Eccezione lanciata");System.out.print("str");
		}

		while(true){
			List<IASTStm> stmttt = new ArrayList<IASTStm>();
			System.out.print(stmttt.toString());
			continue;
		}


		int month = 8;
		String monthString;
		switch (month) {
			case 0:
			case 1:  monthString = "January";
				break;
			case 2:  monthString = "February";
				break;
			case 3:  monthString = "March";
				break;
			case 4:  monthString = "April";
				break;
			case 5:  monthString = "May";
				break;
			case 6:  monthString = "June";
				break;
			case 7:  monthString = "July";
				break;
			case 8:  monthString = "August";
				break;
			case 9:  monthString = "September";
				break;
			case 10: monthString = "October";
				break;
			case 11: monthString = "November";
				break;
			case 12: monthString = "December";
				break;
			default: monthString = "Invalid month";
				break;
		}
		System.out.println(monthString);

		synchronized(monthString) {
			//inside the
			System.out.print(stmttt.toString());
		}

	}
}