package no.met.kvclient.service.kafka;

import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.service.kvDataNotifySubscriber;
import no.met.kvclient.service.kvDataSubscriber;
import no.met.kvclient.service.kvHintSubscriber;
import no.met.kvclient.service.KvSubsribeData;

public class kvKafkaDataSubscriber implements KvSubsribeData {

	@Override
	public SubscribeId subscribeDataNotify(DataSubscribeInfo info, kvDataNotifySubscriber sub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubscribeId subscribeData(DataSubscribeInfo info, kvDataSubscriber sub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubscribeId subscribeKvHint(kvHintSubscriber sub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribe(SubscribeId subid) {
		// TODO Auto-generated method stub

	}

}
