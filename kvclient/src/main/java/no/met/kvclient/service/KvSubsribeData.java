package kvalobs.service;

public interface KvSubsribeData {
	SubscribeId subscribeDataNotify(DataSubscribeInfo info, kvDataNotifySubscriber sub);

	SubscribeId subscribeData(DataSubscribeInfo info, kvDataSubscriber sub);

	SubscribeId subscribeKvHint(kvHintSubscriber sub);

	void unsubscribe(SubscribeId subid);
}
