package no.met.kvclient.service.kafka;

import java.util.Properties;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;
import no.met.kvclient.KvDataSubscribeInfo;
import no.met.kvclient.KvEvent;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.priv.DataSubscribers;
import no.met.kvclient.priv.KvEventQue;
import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.kvDataNotifySubscriber;
import no.met.kvclient.service.kvDataSubscriber;
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
		topic = conf.getProperty("topic", "kvalobs.data");
		groupId = conf.getProperty("group.id");

		if (topic == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("No topic");
		}
		if (groupId == null) {
			System.out.println("No topic");
			throw new IllegalArgumentException("No group.id");
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
		String id = prefix + nextSubscriberId;
		++nextSubscriberId;
		return new SubscribeId(id);
	}

	@Override
	synchronized public SubscribeId subscribeDataNotify(DataSubscribeInfo subscribeInfo,
			KvDataNotifyEventListener listener) {
		SubscribeId subid = getSubscriberId("data_notify_subscriber_");
		dataNotifyList.addSubscriber(subid, subscribeInfo, listener);

		if( !isStarted )
			start();
		
		return subid;
	}

	@Override
	synchronized public SubscribeId subscribeData(DataSubscribeInfo subscribeInfo, KvDataEventListener listener) {
		SubscribeId subid = getSubscriberId("data_subscriber_");
		dataList.addSubscriber(subid, subscribeInfo, listener);

		if( !isStarted )
			start();
		
		return subid;
	}

	@Override
	public SubscribeId subscribeKvHint(kvHintSubscriber sub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	synchronized public void unsubscribe(SubscribeId subid) {
		String id = subid.toString();

		if (id.startsWith("data_notify_subscriber_")) {
			dataNotifyList.remove(subid);
		} else if (id.startsWith("data_subscriber_")) {
			dataList.remove(subid);
		}
		shutdown();
	}

	synchronized void callListeners(Object source, ObsDataList data) {
		if(! dataList.isEmpty())
			dataList.callListeners(source, data);
		if( !dataNotifyList.isEmpty())
			dataNotifyList.callListeners(source, data);
	}

	public void start(boolean delayUntilItHasSubscribers) {
		if (delayUntilItHasSubscribers) {
			if (!dataNotifyList.isEmpty() || !dataList.isEmpty())
				start();
		} else {
			start();
		}
	}

	public void start() {	
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
	
	public void shutdown(){
		if( consumer != null )
			consumer.shutdown();
		if(executor!=null)
			executor.shutdown();
		isStarted=false;
	}
}
