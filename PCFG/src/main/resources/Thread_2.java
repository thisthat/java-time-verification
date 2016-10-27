class Thread_2 extends Thread {
	Object minPrime;
	PrimeThread(Object minPrime) {
		this.minPrime = minPrime;
	}
	public void run() {
		System.out.print("Start the thread");
		int do_smth = this.init();
		synchronized (minPrime){
			minPrime.hashCode();
		}
		System.out.print("Result" + do_smth);
	}
	public synchronized int init(){
		return 10 * 90;
	}
}