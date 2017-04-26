package a.b.c.d.e.test;


public class UndefiniteTimeBehaviour {
	public void method_1(int x){
		Object a = new Object();
		a.wait();
		try{
			long duration_secs = System.currentTimeMillis()/1000;
			if ( duration_secs == x){

				long length = file.getSourceFile().getLength();

				return( length * 10 );

			}  else{
				long mb_per_sec = 3;	// upper limit of 3 MB a sec assumed

				return( duration_secs * mb_per_sec*1024*1024L );
			}
		}catch( Throwable e ){
		}
	}
}