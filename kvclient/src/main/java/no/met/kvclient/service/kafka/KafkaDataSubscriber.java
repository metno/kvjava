package no.met.kvclient.service.kafka;

import java.util.Properties;
import org.apache.kafka.clients.consumer.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;
import no.met.kvclient.KvHintEventListener;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.priv.DataSubscribers;
import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.KvSubsribeData;
import no.met.kvclient.service.ObsDataList;

public class KafkaDataSubscriber implements KvSubsribeData {
	private long nextSubscriberId = 0;
	private KafkaConsumer<String, String> consumer =null;
	private String topic = null;
	private String groupId = null;
	private ExecutorService executor;
	private DataSubscribers<KvDataNotifyEventListener> dataNotifyList;
	private DataSubscribers<KvDataEventListener> dataList;
	private boolean isStarted = false;
	Properties conf=null;
	
	
	Properties createConfig(Properties conf) {
		Properties props = new Properties();
		String brokers=null;
		
		if(conf==null)
			conf =  new Properties();
		
		topic = conf.getProperty("kafks.topic", "kvalobs.production.checked");
		groupId = conf.getProperty("kafka.group.id");
		brokers=conf.getProperty("kafka.connect");
		
		if (topic == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("Kafka: No kafka.topic");
		}
		
		if (groupId == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("Kafka: No kafka.group.id");
		}
		
		if (brokers == null) {
			System.out.println("No kafka brokers to connect is defined.");
			throw new IllegalArgumentException("Kafka: No kafka.connect.brokers defined.");
		}

		props.put("bootstrap.servers", brokers);
		props.put("group.id", groupId);
		props.put("enable.auto.commit", conf.getOrDefault("kafka.auto.commit", "true"));
		props.put("auto.commit.interval.ms", conf.getOrDefault("kafka.auto.commit.interval.ms", "1000"));
		props.put("session.timeout.ms", conf.getOrDefault("kafka.session.timeout.ms", "30000"));
		props.put("max.poll.records", conf.getOrDefault("kafka.max.poll.records", "2"));
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		return props;
	}

	

	public KafkaDataSubscriber(Properties conf, ListenerEventQue defaultQue) {
		dataNotifyList = new DataSubscribers<>(defaultQue);
		dataList = new DataSubscribers<>(defaultQue);

		this.conf = createConfig(conf);
	}

	// Must be called in by a synchronized method
	private SubscribeId getSubscriberId(String prefix) {
		String id = prefix + "-"+ nextSubscriberId;
		++nextSubscriberId;
		return new SubscribeId(id);
	}

	@Override
	synchronized public SubscribeId subscribeDataNotify(DataSubscribeInfo subscribeInfo,
			KvDataNotifyEventListener listener) {
		SubscribeId subid = getSubscriberId("data_notify_subscriber");
		dataNotifyList.addSubscriber(subid, subscribeInfo, listener);
		tryStart();
		
		return subid;
	}

	@Override
	synchronized public SubscribeId subscribeData(DataSubscribeInfo subscribeInfo, KvDataEventListener listener) {
		SubscribeId subid = getSubscriberId("data_subscriber");
		dataList.addSubscriber(subid, subscribeInfo, listener);
		tryStart();
		
		return subid;
	}

	@Override
	synchronized public SubscribeId subscribeKvHint(KvHintEventListener sub) {
		SubscribeId subid=getSubscriberId("hint_subscriber-not_implemented");
		return subid;
	}

	@Override
	synchronized public void unsubscribe(SubscribeId subid) {
		String id = subid.toString();

		if (id.startsWith("data_notify_subscriber-")) {
			dataNotifyList.remove(subid);
		} else if (id.startsWith("data_subscriber-")) {
			dataList.remove(subid);
		} else if( id.startsWith("hint_subscriber-")) {
			//NOT implemented.
		}
		
		if( dataList.isEmpty() && dataNotifyList.isEmpty() )
			stop();
	}

	synchronized void callListeners(Object source, ObsDataList data) throws InterruptedException {
		if(! dataList.isEmpty())
			dataList.callListeners(source, data);
		if( !dataNotifyList.isEmpty())
			dataNotifyList.callListeners(source, data);
	}

	synchronized public void start(boolean delayUntilItHasSubscribers) {
		if (delayUntilItHasSubscribers) {
			if (!dataNotifyList.isEmpty() || !dataList.isEmpty())
				tryStart();
		} else {
			tryStart();
		}
	}

	
	synchronized void tryStart() {	
		if (isStarted)
			return;

		consumer = new KafkaConsumer<>(conf); 
		executor = Executors.newFixedThreadPool(1);
		KvDataConsumer kvConsumer=new KvDataConsumer(consumer, this, topic); 
		executor.submit(kvConsumer);
		System.out.println("*** Kafka: Started.......");
		isStarted=true;
	}
	
	@Override
	public void start(){
		start( true );
	}

	@Override
	synchronized public void stop(){
		System.out.println("**** KafkaDataSubscriber: Try to stop.");
		if( consumer != null )
			consumer.wakeup();
		if(executor!=null)
			executor.shutdown();
		isStarted=false;
		System.out.println("**** KafkaDataSubscriber: Stoped ??????.");
	}


	
}
