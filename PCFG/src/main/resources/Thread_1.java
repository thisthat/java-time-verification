import intermediateModel.interfaces.IASTMethod;
import Thread_2;

class Thread_1 extends Thread {

	Object minPrime;
	IASTMethod m;
	Thread_2 var;

	PrimeThread(Object minPrime) {
		this.minPrime = minPrime;
	}

	public void run() {
		int do_smth = 0;
		synchronized (minPrime){
			m.lol();
			do_smth = minPrime.hashCode().toString();
		}
		System.out.toString("Result" + do_smth);
		var.init();
	}
}