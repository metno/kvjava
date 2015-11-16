package no.met.kvclient.priv;

import java.util.LinkedList;

import no.met.kvclient.KvEventListener;
import no.met.kvclient.service.DataElem;
import no.met.kvclient.service.DataSubscribeInfo;
import no.met.kvclient.service.ObsData;
import no.met.kvclient.service.ObsDataList;
import no.met.kvclient.service.SubscribeId;

public class DataSubscribers<Listener extends KvEventListener>
		extends LinkedList<DataSubscribers<Listener>.SubscriberElem> {
	private static final long serialVersionUID = 1L;

	public class SubscriberElem {
		SubscribeId id;
		DataSubscribeInfo info;
		Listener listener;

		public SubscriberElem(SubscribeId id, DataSubscribeInfo info, Listener listener) {
			this.id = id;
			this.info = info;
			this.listener = listener;
		}
	}

	public DataSubscribers() {
	}

	public void addSubscriber(SubscribeId id, DataSubscribeInfo info, Listener listener) {
		add(new SubscriberElem(id, info, listener));
	}

	ObsDataList filter(ObsDataList data, DataSubscribeInfo info) {
		ObsDataList dataToSend = new ObsDataList();

		for (no.met.kvclient.service.ObsData de : data) {
			ObsData obsData = new ObsData(de.stationid, de.typeID, de.obstime, de.textDataList);

			for (DataElem elem : de.dataList) {
				if (info.has(elem)) {
					obsData.dataList.add(elem);
				}
			}
			if (!(obsData.dataList.isEmpty() && obsData.textDataList.isEmpty())) {
				dataToSend.add(obsData);
			}
		}
		return dataToSend;
	}

	public void callListeners(Object source, ObsDataList data) {
		ObsDataList dataToSend;
		for (SubscriberElem elem : this) {
			dataToSend = filter(data, elem.info);
			if (!dataToSend.isEmpty()) {
				elem.listener.callListener(source, dataToSend);
			}
		}
	}
}
