package no.met.kvclient.service;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;
import no.met.kvutil.LifeCycle;

public interface KvSubsribeData extends LifeCycle{
	SubscribeId subscribeDataNotify(DataSubscribeInfo info, KvDataNotifyEventListener listener);

	SubscribeId subscribeData(DataSubscribeInfo info, KvDataEventListener listener);

	SubscribeId subscribeKvHint(kvHintSubscriber sub);

	void unsubscribe(SubscribeId subid);
	
	void start();
	void stop();
}

