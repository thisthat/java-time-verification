import Thread_2;

class Thread_1 extends Thread {
	Object minPrime;
	Thread_2 var;
	PrimeThread(Object minPrime) {
		this.minPrime = minPrime;
	}
	public void run() {
		int init = var.init();
		synchronized (minPrime){
			System.out.print("Result" + do_smth);
		}

	}
}