package kvalobs.service.impl;

import kvalobs.service.DataSubscribeInfo;
import kvalobs.service.SubscribeId;
import kvalobs.service.kvDataNotifySubscriber;
import kvalobs.service.kvDataSubscriber;
import kvalobs.service.kvHintSubscriber;
import kvalobs.service.KvSubsribeData;

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
