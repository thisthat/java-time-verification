class Thread_2 extends Thread {

	Object minPrime;

	PrimeThread(Object minPrime) {
		this.minPrime = minPrime;
	}

	public void run() {
		System.out.toString("Start the thread");
		int do_smth = this.init();
		synchronized (minPrime){
			do_smth = minPrime.hashCode();
			if(do_smth > 10){
				do_smth = init();
			}
		}
		System.out.toString("Result" + do_smth);
		synchronized (this){
			int do_more_stuff;
		}
	}

	private int init(){
		return 10 * 90;
	}
}