package no.met.kvalobs.kafkatest;

import no.met.kvutil.concurrent.ThreadManager;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.kafka.KafkaDataSubscriber;
import no.met.kvclient.kafka.KafkaApp;
public class KafkaAppTest {
	
	static class Hook extends Thread {
		ThreadManager mgr;
		AtomicBoolean shutdown;
		KafkaDataSubscriber kafka;
		public Hook(ThreadManager mgr, AtomicBoolean shutdown, KafkaDataSubscriber kafka){
			this.mgr=mgr;
			this.shutdown = shutdown;
			this.kafka = kafka;
		}
		@Override
		public void run() {
			System.err.println("Shutdown - start");
			
			
			try{
				shutdown.compareAndSet(false, true);
				kafka.stop();
				Thread.sleep(2000);
				mgr.shutdown();
				mgr.join(10000);
			}
			catch(InterruptedException ex){
			}
			System.err.println("Threads remaining: " + mgr.size());
			System.err.println("Shutdown - completed");
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Properties prop=new Properties();
		prop.setProperty("group.id", "kvclient-borge-ea29af5c-1699-4299-a4dd-8cc272319436");
		prop.setProperty("kvalobs.subscribe.workers", "3");
		KafkaApp app = KafkaApp.factory( prop );
		DataSubscriber dataSub=new DataSubscriber();
		
		SubscribeId subid = app.subscribeData(null, dataSub );
		
		if( app == null ) {
			System.err.println("Fatal: Could not create app");
			System.exit(1);
		}
		
		System.err.println("Subscriberid: " + subid);
		
		try{
			app.run();
		}
		catch (InterruptedException ex){
			System.err.println("Main app interupted.");
		}
		
	}

}
