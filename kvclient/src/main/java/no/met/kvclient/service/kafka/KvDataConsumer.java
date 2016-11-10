package no.met.kvclient.service.kafka;

import java.util.Arrays;
import java.util.Map;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.*;
import no.met.kvclient.kv2kvxml.FormatException;
import no.met.kvclient.kv2kvxml.Kv2KvXml;
import no.met.kvclient.service.ObsDataList;

public class KvDataConsumer implements Runnable {
	
	private KafkaConsumer<String,String> consumer =null;
	private String topic;
	KafkaDataSubscriber subscribers;
	int retryCount=0;

	public KvDataConsumer(KafkaConsumer<String, String> consumer, KafkaDataSubscriber subscribers, String topic) {
		this.consumer = consumer;
		this.topic = topic;
		this.subscribers = subscribers;
	}
	
	void doConsume() throws InterruptedException{
		consumer.subscribe(Arrays.asList(topic));

		while(true) {
			ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
			if (records.isEmpty())
				continue;

			retryCount = 0;
			for (ConsumerRecord<String, String> record : records) {
				System.out.println("KvDataConsumer: BEGIN  '" + topic + "'.");
				System.out.printf("KvDataConsumer: offset = %d, key = %s", record.offset(), record.key());
				System.out.println("KvDataConsumer: value\n" + record.value());
				System.out.println("KvDataConsumer: -----------   END ---------------------");
				try {
					ObsDataList data = Kv2KvXml.decodeFromString(record.value());
					subscribers.callListeners(this, data);
				} catch (FormatException ex) {
					System.err.println(ex.getMessage());
				}
			}
		}
	}

	boolean consume() {
		try {
			doConsume();
		} 
		catch (InterruptedException ex) {
			System.err.println("KvDataConsumer: InterruptedException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
			
		}
		catch( InvalidOffsetException ex) {
			System.err.println("KvDataConsumer: InvalidOffsetException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return true;
		}
		catch( WakeupException ex ) {
			System.err.println("KvDataConsumer: InterruptedException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
		}
		catch(AuthorizationException ex ){
			System.err.println("KvDataConsumer: FATAL: AuthorizationException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
		}
		catch(KafkaException ex) {
			System.err.println("KvDataConsumer: KafkaException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return true;
		}
		catch (Exception ex) {
			System.out.println("****** KvDataConsumer.Exception: " + ex.getMessage());
			return false;
		}
		return false;
	}
	
	public void run() {
		try  {
			while( consume() && retryCount < 3 ) {
				consumer.close();
				Thread.sleep(10000);  // sleep 10 seconds before retry.
				
			}
		} catch (InterruptedException ex ){
			System.err.println("****** KvDataConsumer: InteruptedException: " + ex.getMessage());
		}
			
		consumer.close();
		System.out.println("Shutting down KvDataConsumer Thread: " + topic);
	}
}
