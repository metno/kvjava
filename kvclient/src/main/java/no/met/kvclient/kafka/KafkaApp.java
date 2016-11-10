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
import no.met.kvutil.ProcUtil;
import no.met.kvutil.PropertiesHelper;
import no.met.kvutil.concurrent.ThreadManager;

public class KafkaApp extends kvService {
	PropertiesHelper prop;
	ListenerEventQue que;
	AtomicBoolean doShutdown;
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
			System.err.println("*** KafkaApp.Hook: Shutdown - start\n" +  ProcUtil.getStackTrace());
			try{
				app.onExit();
				app.shutdown();
			}
			catch(InterruptedException ex){
			}
			catch( Exception ex ){
				System.err.println(" ------ Exception in KafkaApp.Hook: " + ex.getMessage());
				ex.printStackTrace();
			}
			System.err.println("*** KafkaApp.Hook: Shutdown - completed");
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
		PropertiesHelper prop=PropertiesHelper.loadFile(conf);
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
	
	
	
	public KafkaApp(Properties prop) {
		this( prop, new ListenerEventQue(Math.abs(Integer.parseInt(prop.getProperty("que.size", "10")))));
		KafkaDataSubscriber sub = null;
		SqlDataQuery query=null;
		
		try {
			sub = new KafkaDataSubscriber(prop, que);
			query = new SqlDataQuery(prop,que);
			this.init(sub, query);
		}
		catch( Exception ex){
			System.err.println("Configuration error: " + ex.getMessage());
			
			if( sub != null )
				sub.stop();
			
			if(query !=  null )
				query.stop();
			
			throw new InternalError();
		}
	}
	
	KafkaApp(Properties prop, ListenerEventQue que) {
		this.que=que;
		this.prop=new PropertiesHelper(prop);
		doShutdown = new AtomicBoolean(false);
		workers=new ThreadManager("Subscribers");
		subscriberWorkers = Math.abs(Integer.parseInt(prop.getProperty("kvalobs.subscribe.workers", "1")));
		
		for( int i=0; i<subscriberWorkers; ++i){
			workers.start(new ListenerEventRunner(doShutdown, que));
		}
		
		isInterupted = false;
	}
	
	public KafkaApp(Properties prop, ListenerEventQue que, KvDataQuery query, KvSubsribeData subscribers ){
		this(prop, que);
		init(subscribers, query);
	}
	
	public PropertiesHelper getConf() { 
		return prop;
	}
	public AtomicBoolean getShutdown() {
		return doShutdown;
	}
	
	
	public ListenerEventQue getQue() {
		return que;
	}
	
	
	public void shutdown()throws InterruptedException {
		System.out.println("***** Shutdown *****\n" + ProcUtil.getStackTrace());
		
		if(doShutdown.compareAndSet(false, true)){
			stop();
			workers.shutdown();
			workers.join(10000);
		}
	}
	
	protected void onExit(){
		
	}
	
	protected void onStartup(){
		
	}
	public void run() throws InterruptedException {
		System.err.println("Starting main thread, workers " + workers.size()+".");
		Runtime.getRuntime().addShutdownHook(new Hook(this));
		onStartup();
		start();
		while(!doShutdown.get() && ! isInterupted){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				isInterupted = true;
			}
		}
		
		System.err.println("Try to shutdown the kafka service an close the database connections to kvalobs");
		shutdown();
		onExit();
		System.err.println("Terminating main thread, workers still alive " + workers.size() + " (interupted: " + (isInterupted?"true":"false")+").");
	}
	
}
