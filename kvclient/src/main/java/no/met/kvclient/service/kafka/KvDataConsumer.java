package no.met.kvclient.service.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import no.met.kvclient.kv2kvxml.FormatException;
import no.met.kvclient.kv2kvxml.Kv2KvXml;
import no.met.kvclient.service.ObsDataList;

public class KvDataConsumer implements Runnable {
	private KafkaStream<byte[], byte[]> stream;
	private String topic;
	KafkaDataSubscriber subscribers;

	public KvDataConsumer(KafkaStream<byte[], byte[]> stream, KafkaDataSubscriber subscribers, String topic) {
		this.stream = stream;
		this.topic = topic;
		this.subscribers = subscribers;
	}

	public void run() {
		ConsumerIterator<byte[], byte[]> it = stream.iterator();
		try {
			while (it.hasNext()) {
				String msg = new String(it.next().message());
//				System.out.println("Thread '" + topic + "'.\n" + msg);
//				System.out.println("--------------------------------------");
				try {
					ObsDataList data = Kv2KvXml.decodeFromString(msg);
					subscribers.callListeners(this, data);
				} catch (FormatException ex) {
					System.err.println(ex.getMessage());
				}
			}
			
			System.err.println("****** it.hasNext() == false");
		} 
		catch (InterruptedException ex) {
			System.err.println("Interupted thread: " + topic);
		}
		System.out.println("Shutting down Thread: " + topic);
	}
}
