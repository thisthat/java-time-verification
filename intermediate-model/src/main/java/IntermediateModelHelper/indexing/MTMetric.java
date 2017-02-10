package IntermediateModelHelper.indexing;


import java.util.HashMap;

public class MTMetric {

	class Metric {
		int thread,locks,methods;

		public int getThread() {
			return thread;
		}

		public void setThread(int thread) {
			this.thread = thread;
		}

		public int getLocks() {
			return locks;
		}

		public void setLocks(int locks) {
			this.locks = locks;
		}

		public int getMethods() {
			return methods;
		}

		public void setMethods(int methods) {
			this.methods = methods;
		}
	}
	//thread
	//locks
	//methods

	HashMap<String,Metric> metrics = new HashMap<>();

	void setThread(String _className, int nthread){
		Metric working;
		if(metrics.containsKey(_className)){
			working = metrics.get(_className);
		} else {
			working = new Metric();
		}
		working.setThread(nthread);
	}
	void setLocks(String _className, int nLocks){
		Metric working;
		if(metrics.containsKey(_className)){
			working = metrics.get(_className);
		} else {
			working = new Metric();
		}
		working.setLocks(nLocks);
	}
	void setMethods(String _className, int nMethod){
		Metric working;
		if(metrics.containsKey(_className)){
			working = metrics.get(_className);
		} else {
			working = new Metric();
		}
		working.setMethods(nMethod);
	}

	public int getThreadNumber(){
		int out = 0;
		for(String _className : metrics.keySet()){
			out += metrics.get(_className).getThread();
		}
		return out;
	}
	public int getLockNumber(){
		int out = 0;
		for(String _className : metrics.keySet()){
			out += metrics.get(_className).getLocks();
		}
		return out;
	}
	public int getMethodNumber(){
		int out = 0;
		for(String _className : metrics.keySet()){
			out += metrics.get(_className).getMethods();
		}
		return out;
	}
	public int getTotalNumber(){
		int out = 0;
		for(String _className : metrics.keySet()){
			out += metrics.get(_className).getThread();
			out += metrics.get(_className).getLocks();
			out += metrics.get(_className).getMethods();
		}
		return out;
	}

}
