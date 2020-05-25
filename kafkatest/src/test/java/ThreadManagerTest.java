import java.time.Duration;
import java.time.Instant;
import java.lang.Integer;


public class ThreadManagerTest {
	
	static class Hook extends Thread {
		ThreadManager mgr;
		public Hook(ThreadManager mgr){
			this.mgr=mgr;
		}
		@Override
		public void run() {
			System.err.println("Shutdown - start");
			mgr.shutdown(10000);
			System.err.println("Threads remaining: " + mgr.size());
			System.err.println("Shutdown - completed");
		}
	}
	
	public static void main(String[] args) {
		ThreadManager mgr=new ThreadManager("MY");
		
		Runtime.getRuntime().addShutdownHook(new Hook(mgr));
		
		for( int i=0; i<10; ++i) {
			mgr.start( new Runnable() {
				@Override
				public void run() {
					try {
						while(true){
							Thread.sleep(1000);
						}
					} catch (InterruptedException e) {
						System.err.println("Interupted: " + Thread.currentThread().getName());
					}

				}
			});
		}
		
		System.out.println("Thread count: " + mgr.size());
		
		try {
			Thread.sleep(10000);
			mgr.shutdown(2000);
			System.out.println("Thread count: " + mgr.size());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
