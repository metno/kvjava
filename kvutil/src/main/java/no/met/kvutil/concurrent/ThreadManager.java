package no.met.kvutil.concurrent;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager {

	ThreadGroup thrGroup;
	AtomicInteger numberOfThreads;
	AtomicInteger threadId;
	AtomicBoolean shutDown;
	
	ThreadManager(String name){
		thrGroup = new ThreadGroup(name);
		numberOfThreads=new AtomicInteger(0);
		shutDown=new AtomicBoolean(false);
		threadId = new AtomicInteger(0);
	}
	
	class Runner extends Thread {
		ThreadManager manager;
		Runnable toRun;
		
		Runner( ThreadManager mgr, Runnable run, String name){
			super(mgr.thrGroup, name);
			setDaemon(true);
			manager=mgr;
			toRun=run;
			start();
		}
		
		@Override
		public void run() {
			manager.numberOfThreads.incrementAndGet();
			try {
				toRun.run();
			}
			catch(Exception ex ){
				System.err.println("Ucaught exception: "+manager.thrGroup.getName());
			}
			manager.numberOfThreads.decrementAndGet();
		}
		
	}
	
	
	public void shutdown( long waitInMillisTo){
		if( shutDown.compareAndSet(false, true) )
			thrGroup.interrupt();
	}
	
	/**
	 * 
	 * @param waitInMillis wait for this time to all 
	 * therads has terminated. 0 to wait indefinitely.
	 * 
	 * @return Number of threads that has not stopped while waiting.
	 */
	public int join(long waitInMillisTo) throws InterruptedException {
		Instant now=Instant.now();
		Instant stop=waitInMillisTo>0?now.plusMillis(waitInMillisTo):Instant.MAX;
		
		while( numberOfThreads.get()>0 && now.compareTo(stop)<=0 ) {
			Thread.sleep(100);
			now=Instant.now();
		}
		
		return numberOfThreads.get();
	}
	
	public void start( Runnable runable){
		start(runable, "");
	}
	
	public void start( Runnable runable, String name){
		if( shutDown.get() )
			throw new IllegalStateException("The thread manager '" + thrGroup.getName()+"' is shutdown.");
		
		String theName= thrGroup.getName()+":"+name+":"+threadId.getAndIncrement();
		new Runner(this, runable, theName);
	}
	
	public int size(){ return numberOfThreads.get();}
	
}


