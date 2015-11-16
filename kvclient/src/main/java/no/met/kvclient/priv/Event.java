package kvalobs.priv;

import kvalobs.KvDataEventListener;
import kvalobs.service.ObsDataList;

public class Event {
	public enum Hint {
		Up, Down
	};

	public enum Type {
		DataEvent, HintEvent, GetDataEvent
	};

	Type eventType;
	ObsDataList data = null;
	Hint hint = Hint.Down;
	KvDataEventListener listener = null;
	Object source;

	public Event(Object source, ObsDataList data) {
		this.source = source;
		eventType = Type.DataEvent;
		this.data = data;
	}

	public Event(Object source, Hint hint) {
		this.source = source;
		eventType = Type.HintEvent;
		this.hint = hint;
	}

	public Event(Object source, ObsDataList data, KvDataEventListener listener) {
		this.source = source;
		eventType = Type.HintEvent;
		this.hint = Hint.Down;
		this.data = null;
	}

	public Object getSource() {
		return source;
	}

	public Type getEventType() {
		return eventType;
	}

	public ObsDataList getObsData() throws IllegalStateException {
		if (eventType == Type.DataEvent || eventType == Type.GetDataEvent)
			return data;

		throw new IllegalStateException("IllegalStateException: getObsData: EventType is '" + eventType + "'.");
	}

	public Hint getHint() throws IllegalStateException {
		if (eventType != Type.DataEvent)
			throw new IllegalStateException("IllegalStateException: getHint: EventType is '" + eventType + "'.");
		return hint;
	}

	public KvDataEventListener getGetDataEventListener() throws IllegalStateException {
		if (eventType != Type.GetDataEvent)
			throw new IllegalStateException("IllegalStateException: getHint: EventType is '" + eventType + "'.");
		return listener;
	}
}