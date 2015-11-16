package no.met.kvclient.priv;

import java.util.LinkedList;

import no.met.kvclient.service.SubscribeId;
import no.met.kvclient.KvHintEventListener;

public class HintSubscribers extends LinkedList<HintSubscribers.SubscriberElem> {
	private static final long serialVersionUID = 445868070046645018L;

	public class SubscriberElem {
		SubscribeId id;
		KvHintEventListener listener;

		public SubscriberElem(SubscribeId id, KvHintEventListener listener) {
			this.id = id;
			this.listener = listener;
		}
	}

	public HintSubscribers() {
	}

	public void addSubscriber(SubscribeId id, KvHintEventListener listener) {
		add(new SubscriberElem(id, listener));
	}

	public void callListeners(Object source, Event.Hint hint) {
		for (SubscriberElem elem : this) {
			switch (hint) {
			case Up:
				elem.listener.up();
				break;
			case Down:
				elem.listener.down();
				break;
			}
		}
	}
}
