import intermediateModel.interfaces.IASTMethod;

class Thread_1 extends Thread {

	Object minPrime;
	IASTMethod m;

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
	}
}