import IntermediateModelHelper.envirorment.Env;
import Thread_2;
import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.interfaces.ParseIM;

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

	public void example1(){
		int init = var.init();
		synchronized (minPrime){
			System.out.print("Result" + do_smth);
			var.init();
		}
		for(int i : list){
			System.out.println(i);
		}
		var xyz = new ParseIM(){
			@Override
			protected void analyzeMethod(IASTMethod method, Env e) {
				super.analyzeMethod(method, e);
			}

			@Override
			public void start(ASTClass c) {
				super.start(c);
			}
		};
		xyz.start();
	}
}