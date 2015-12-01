package no.met.kvclient.service.kafka;

import java.util.Properties;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.priv.DataSubscribers;
import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.kvHintSubscriber;
import no.met.kvclient.service.KvSubsribeData;
import no.met.kvclient.service.ObsDataList;

public class KafkaDataSubscriber implements KvSubsribeData {
	private long nextSubscriberId = 0;
	private ConsumerConnector consumer;
	private String topic = null;
	private String groupId = null;
	private ExecutorService executor;
	private DataSubscribers<KvDataNotifyEventListener> dataNotifyList;
	private DataSubscribers<KvDataEventListener> dataList;
	private boolean isStarted = false;
	ConsumerConfig conf=null;
	
	ConsumerConfig createConfig(Properties conf) {
		Properties props = new Properties();
		
		if(conf==null)
			conf =  new Properties();
		
		topic = conf.getProperty("topic", "kvalobs.data");
		groupId = conf.getProperty("group.id");

		if (topic == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("Kafka: No topic");
		}
		if (groupId == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("Kafka: No group.id");
		}

		props.put("zookeeper.connect", conf.getOrDefault("zookeeper.connect", "10.99.2.228:2181"));
		props.put("group.id", conf.getProperty("group.id", "kvclient-borge-ea29af5c-1699-4299-a4dd-8cc272319436"));
		props.put("zookeeper.session.timeout.ms", conf.getProperty("zookeeper.session.timeout.ms", "500"));
		props.put("zookeeper.sync.time.ms", conf.getProperty("zookeeper.sync.time.ms", "250"));
		props.put("auto.commit.interval.ms", conf.getProperty("auto.commit.interval.ms", "1000"));
		topic = conf.getProperty("topic", "kvalobs.data");
		return new ConsumerConfig(props);
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
	synchronized public SubscribeId subscribeKvHint(kvHintSubscriber sub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	synchronized public void unsubscribe(SubscribeId subid) {
		String id = subid.toString();

		if (id.startsWith("data_notify_subscriber-")) {
			dataNotifyList.remove(subid);
		} else if (id.startsWith("data_subscriber-")) {
			dataList.remove(subid);
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
		
		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(conf);
		Map<String, Integer> topicMap = new HashMap<String, Integer>();
		topicMap.put(topic,new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreamsMap = consumer.createMessageStreams(topicMap);
		List<KafkaStream<byte[], byte[]>> streamList =  consumerStreamsMap.get(topic);
	 
		//There should be only one stream.
		if( streamList.isEmpty() ) {
			System.err.println("No topic <"+topic+"> defined?");
			return;
		}
		executor = Executors.newFixedThreadPool(1);
		KvDataConsumer kvConsumer=new KvDataConsumer(streamList.get(0), this, topic); 
		executor.submit(kvConsumer);
		isStarted=true;
	}
	
	@Override
	public void start(){
		start( true );
	}

	@Override
	synchronized public void stop(){
		if( consumer != null )
			consumer.shutdown();
		if(executor!=null)
			executor.shutdown();
		isStarted=false;
	}


	
}
