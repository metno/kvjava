package no.met.kvclient.service;

import no.met.kvclient.KvDataEventListener;
import no.met.kvclient.KvDataNotifyEventListener;
import no.met.kvclient.KvHintEventListener;
import no.met.kvutil.LifeCycle;
import no.met.kvutil.PropertiesHelper;

public interface KvSubsribeData extends LifeCycle{
	SubscribeId subscribeDataNotify(DataSubscribeInfo info, KvDataNotifyEventListener listener);

	SubscribeId subscribeData(DataSubscribeInfo info, KvDataEventListener listener);

	SubscribeId subscribeKvHint(KvHintEventListener sub);

	void unsubscribe(SubscribeId subid);

	PropertiesHelper getInfo();

	void start();
	void stop();
}

