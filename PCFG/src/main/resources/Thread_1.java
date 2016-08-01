class Thread_1 extends Thread {

	Object minPrime;

	PrimeThread(Object minPrime) {
		this.minPrime = minPrime;
	}

	public void run() {
		int do_smth = 0;
		synchronized (minPrime){
			do_smth = minPrime.hashCode();
		}
		System.out.toString("Result" + do_smth);
	}
}