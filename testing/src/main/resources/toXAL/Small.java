public class Small {

	public void m1(){
		while(true){

			Runnable toRun;

			synchronized( initQueue ){

				if( initQueue.isEmpty()){

					break;
				}

				toRun = (Runnable)initQueue.remove(0);
			}

			try{
				toRun.run();

			}catch( Throwable e ){

				Debug.out(e);
			}
		}
		return;
	}
}