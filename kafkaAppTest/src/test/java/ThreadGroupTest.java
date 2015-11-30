import java.time.Duration;
import java.time.Instant;
import java.lang.Integer;
import javax.print.attribute.IntegerSyntax;


public class ThreadGroupTest {
	

	
	public static void main(String[] args) {
		ThreadGroup tgrp=new ThreadGroup("TestGroup");
		
		for( int i=0; i<10; ++i) {
			final int ii=i;
			new Thread(tgrp, new Runnable() {
				@Override
				public void run() {
					Instant start=Instant.now();
					final int id=ii;
					long toSleep=Math.round(Math.random()*10000);
					System.err.println("Starting thread: " + id + " sleep: "+((double)toSleep/1000)+" s.");
					try {
						Thread.sleep(toSleep);
						Instant stop=Instant.now();
						System.out.println("exiting thread: " + id +" " + ((double)toSleep/1000) +" s (" +((double)Duration.between(start,stop).toMillis()/1000)+" s).");
					} catch (InterruptedException e) {
						Instant stop=Instant.now();
						System.out.println("Interupted thread: " + id +" (" +Duration.between(start,stop).toMillis()+" ms).");
					}
				
				}
			}).start();
		}

//		tgrp.interrupt();
		for(int i=0; i<13; ++i ){
			System.out.println("Thread count: " + tgrp.activeCount());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Opppsss ...... interupted.");
			}
		}
		
		Integer i=new Integer(0);
		
		++i;
		System.err.println("i: "+i);
		

	}

}
