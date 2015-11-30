package no.met.kvalobs.kafkatest;

import no.met.kvclient.KvDataEvent;
import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.ListenerEventRunner;
import no.met.kvclient.service.ObsDataList;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataSubscriber extends ListenerEventRunner implements KvDataEventListener {
	

	DataSubscriber(AtomicBoolean shutdown, ListenerEventQue que ){
		super( shutdown, que);
	}
	
	public void kvDataEvent(KvDataEvent event){
		Instant now = Instant.now();
		System.out.println("================ " + now + " [BEGIN] ================");
		ObsDataList data=event.getObsData();
		System.out.println(data);
		System.out.println("====================  [END]  ===========================");
	}
}
