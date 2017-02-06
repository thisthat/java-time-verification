import Thread_2;
class Thread_1 extends Thread {
	Object lock;
	Thread_2 var;
	Thread_1(Object lock){
		this.lock = lock;
	}
	public void run(){
		int init = var.init();
		synchronized(lock){
			System.out.print("Thread1");
		}
		Thread.sleep(1000);
		System.out.print("End");
	}
}