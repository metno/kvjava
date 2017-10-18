package no.met.kvclient.priv;

import java.util.LinkedList;

import no.met.kvclient.KvEventListener;
import no.met.kvclient.ListenerEvent;
import no.met.kvclient.ListenerEventQue;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.ObsData;
import no.met.kvclient.service.ObsDataList;
import no.met.kvclient.service.SubscribeId;

public class DataSubscribers<Listener extends KvEventListener>
		extends LinkedList<DataSubscribers<Listener>.SubscriberElem> {
	private static final long serialVersionUID = 1L;
	ListenerEventQue defaultQue;

	public class SubscriberElem {
		SubscribeId id;
		DataSubscribeInfo info;
		Listener listener;
		boolean queIsDead;

		public SubscriberElem(SubscribeId id, DataSubscribeInfo info, Listener listener) {
			this.id = id;
			this.info = info;
			this.listener = listener;
		}

		public String getId() {
			return id.toString();
		}
	}

	public void remove(SubscribeId subid) {
		String id = subid.toString();
		int index = 0;
		for (DataSubscribers<Listener>.SubscriberElem e : this) {
			if (e.getId().compareTo(id) == 0) {
				remove(index);
				break;
			}
			index++;
		}
	}

	public DataSubscribers(ListenerEventQue defaultQue) {
		if (defaultQue == null)
			throw new IllegalArgumentException("defaultQue cant be null.");
		this.defaultQue = defaultQue;
	}

	public void addSubscriber(SubscribeId id, DataSubscribeInfo info, Listener listener) {
		if( info == null )
			info = new DataSubscribeInfo();
		
		if(info.que == null)
			info.que = defaultQue;

		add(new SubscriberElem(id, info, listener));
	}

	ObsDataList filter(ObsDataList data, DataSubscribeInfo info) {
		ObsDataList dataToSend = new ObsDataList();

		for (no.met.kvclient.service.ObsData de : data) {
			ObsData obsData = new ObsData(de.stationID, de.typeID, de.obstime, de.textDataList);

			for (DataElem elem : de.dataList) {
				if( elem.paramID<=0) // Invalid parameterid
					continue;

				if (! info.has(elem))
					continue;

				obsData.dataList.add(elem);
			}

			if (!(obsData.dataList.isEmpty() && obsData.textDataList.isEmpty())) {
				dataToSend.add(obsData);
			}
		}
		return dataToSend;
	}

	public void callListeners(Object source, ObsDataList data) throws InterruptedException {
		for (SubscriberElem elem : this) {
			ObsDataList dataToSend = filter(data, elem.info);
			if (!dataToSend.isEmpty()) {
				elem.info.que.putObject(new ListenerEvent() {
					@Override
					public void run() {
						elem.listener.callListener(source, elem.id, dataToSend);
					}
				});
			}
		}
	}
}
