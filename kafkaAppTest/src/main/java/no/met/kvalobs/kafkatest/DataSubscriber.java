package no.met.kvalobs.kafkatest;

import no.met.kvclient.KvDataEvent;
import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.service.ObsDataList;
import java.time.Instant;

public class DataSubscriber implements KvDataEventListener {
	DataSubscriber(){
	}
	
	public void kvDataEvent(KvDataEvent event){
		Instant now = Instant.now();
		System.out.println("================ " + now + " [BEGIN] ================");
		System.out.println("----- Thread: " + Thread.currentThread().getName() +" -----");
		ObsDataList data=event.getObsData();
		System.out.println(data);
		System.out.println("====================  [END]  ===========================");
	}
}
