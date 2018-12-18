package no.met.kvclient.service.kafka;

import java.util.Arrays;
import java.util.Map;

import no.met.kvclient.service.DataIdElement;
import no.met.kvclient.service.ObsData;
import no.met.kvutil.DateTimeUtil;
import no.met.kvutil.FileUtil;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.*;
import no.met.kvclient.kv2kvxml.FormatException;
import no.met.kvclient.kv2kvxml.Kv2KvXml;
import no.met.kvclient.service.ObsDataList;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class KvDataConsumer implements Runnable {
	static Logger receivelog = Logger.getLogger("receive");
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
				receivelog.debug("topic '" + topic + "' offset = " + record.offset()+ " key = '" + record.key() + "'");

				try {
					ObsDataList data = Kv2KvXml.decodeFromString(record.value());


					for (DataIdElement e : data.keySet()) {
						ObsData od = data.get(e);
						String toLog = e.stationID + ", " + e.typeID + ", " + DateTimeUtil.toString(e.obstime, DateTimeUtil.FMT_PARSE);

						if (od == null) {
							toLog += ", (null)";
						} else {
							toLog += ", #d: " + od.dataList.size() + ", #td: " + od.textDataList.size();
						}
						receivelog.debug(toLog);
					}

					//Debug
//					for(DataIdElement e : data.keySet()) {
//						if( e.stationID==12550 && e.typeID==502) {
//							String out="------- stationid: " + e.stationID + " typeid: " + e.typeID + " received: " + DateTimeUtil.nowToString() + "---\n";
//							out += "----   decoded ----- \n" + data + "\n";
//							out += record.value() + "------------- END -----------------\n";
//							FileUtil.appendStr2File("level_debug.txt", out);
//						}
//					}

					subscribers.callListeners(this, data);
				} catch (FormatException ex) {
					receivelog.error("XML parse error: " + ex.getMessage());
				}
			}
		}
	}

	boolean consume() {
		try {
			doConsume();
		} 
		catch (InterruptedException ex) {
			receivelog.warn("KvDataConsumer: InterruptedException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
			
		}
		catch( InvalidOffsetException ex) {
			receivelog.warn("KvDataConsumer: InvalidOffsetException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return true;
		}
		catch( WakeupException ex ) {
			receivelog.warn("KvDataConsumer: WakeupException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
		}
		catch(AuthorizationException ex ){
			receivelog.error("KvDataConsumer: FATAL: AuthorizationException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return false;
		}
		catch(KafkaException ex) {
			receivelog.warn("KvDataConsumer: KafkaException: topic '" + topic + "'. Reason: " + ex.getMessage());
			return true;
		}
		catch (Exception ex) {
			receivelog.warn("****** KvDataConsumer.Exception: " + ex.getMessage());
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
			receivelog.info("****** KvDataConsumer: InteruptedException: " + ex.getMessage());
		}
			
		consumer.close();
		receivelog.info("Shutting down KvDataConsumer Thread: " + topic);
		System.out.println("Shutting down KvDataConsumer Thread: " + topic);
	}
}
