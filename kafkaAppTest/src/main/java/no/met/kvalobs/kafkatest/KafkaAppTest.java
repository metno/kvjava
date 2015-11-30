package no.met.kvalobs.kafkatest;

import no.met.kvutil.concurrent.ThreadManager;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.kafka.KafkaDataSubscriber;;
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
	
	public static void main(String[] args) {
		Properties prop=new Properties();
		prop.setProperty("group.id", "kvclient-borge-ea29af5c-1699-4299-a4dd-8cc272319436");
		AtomicBoolean shutdown=new AtomicBoolean(false);
		ListenerEventQue defaultQue=new ListenerEventQue(10);
		ThreadManager mgrManager = new ThreadManager("KafkListeners");
		KafkaDataSubscriber kafka=new KafkaDataSubscriber(prop, defaultQue);
		DataSubscriber dataSub=new DataSubscriber(shutdown, defaultQue);
		
		SubscribeId subid = kafka.subscribeData(null, dataSub );
		System.err.println("Subscriberid: " + subid);
		
		mgrManager.start(dataSub, subid.toString());
		
		
		kafka.start();
		
		Runtime.getRuntime().addShutdownHook(new Hook(mgrManager,shutdown, kafka));
		
		try {
			while( true ) {
				Thread.sleep(Long.MAX_VALUE);
			}
		} catch (InterruptedException e) {
			System.out.println("Terminating............ remaining threads: " + mgrManager.size());
		}
		
		
	}

}
