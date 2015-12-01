package no.met.kvclient.kafka;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.ListenerEventRunner;
import no.met.kvclient.service.KvDataQuery;
import no.met.kvclient.service.KvSubsribeData;
import no.met.kvclient.service.kvService;
import no.met.kvclient.service.kafka.KafkaDataSubscriber;
import no.met.kvclient.service.sql.SqlDataQuery;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.concurrent.ThreadManager;

public class KafkaApp extends kvService {
	Properties prop;
	ListenerEventQue que;
	AtomicBoolean shutdown;
	boolean isInterupted;
	int subscriberWorkers;
	ThreadManager workers;
		
	
	static class Hook extends Thread {
		KafkaApp app;
		public Hook(KafkaApp app){
			this.app=app;
		}
		@Override
		public void run() {
			System.err.println("Shutdown - start");
			try{
				app.shutdown();
			}
			catch(InterruptedException ex){
			}
			System.err.println("Shutdown - completed");
		}
	}
	
	
	static public KafkaApp factory(Properties prop) {
		ListenerEventQue que = new ListenerEventQue(Math.abs(Integer.parseInt(prop.getProperty("que.size", "10"))));
		KafkaDataSubscriber sub = null;
		SqlDataQuery query=null;
		
		try {
			sub = new KafkaDataSubscriber(prop, que);
			query = new SqlDataQuery(prop,que);
			return new KafkaApp(prop, que, query, sub);
		}
		catch( Exception ex){
			System.err.println("Configuration error: " + ex.getMessage());
			
			if( sub != null )
				sub.stop();
			
			if(query !=  null )
				query.stop();
			
			return null;
		}
	}
	
	static public KafkaApp factory(Path conf) {
		Properties prop=PropertiesHelper.loadFile(conf);
		if( prop == null)
			return null;
		
		return factory(prop);
	}

	static public KafkaApp factory(String conf) {
		Properties prop = PropertiesHelper.loadFile(conf);
		if( prop == null )
			return null;
		
		return factory(prop);
	}
	
	public KafkaApp(Properties prop, ListenerEventQue que, KvDataQuery query, KvSubsribeData subscribers ){
		super(subscribers, query);
		this.que=que;
		this.prop=prop;
		shutdown = new AtomicBoolean(false);
		workers=new ThreadManager("Subscribers");
		subscriberWorkers = Math.abs(Integer.parseInt(prop.getProperty("kvalobs.subscribe.workers", "1")));
		
		Runtime.getRuntime().addShutdownHook(new Hook(this));
		
		for( int i=0; i<subscriberWorkers; ++i){
			workers.start(new ListenerEventRunner(shutdown, que));
		}
		
		isInterupted = false;
	}
	
	public Properties getConf() { 
		return prop;
	}
	public AtomicBoolean getShutdown() {
		return shutdown;
	}
	
	
	public ListenerEventQue getQue() {
		return que;
	}
	
	
	public void shutdown()throws InterruptedException {
		if(shutdown.compareAndSet(false, true)){
			stop();
			workers.shutdown();
			workers.join(10000);
		}
	}
	
	public void run() throws InterruptedException {
		System.err.println("Starting main thread, workers " + workers.size()+".");
		start();
		while(!shutdown.get() && ! isInterupted){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				isInterupted = true;
				shutdown();
			}
		}
		
		System.err.println("Terminating main thread, workers still alive " + workers.size() + " (interupted: " + (isInterupted?"true":"false")+").");
	}
	
}
