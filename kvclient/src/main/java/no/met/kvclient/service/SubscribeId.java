package no.met.kvclient.service;

public class SubscribeId implements Comparable<SubscribeId> {
	public String id = null;

	public SubscribeId() {
		this("");
	}

	public SubscribeId(String id) {
		this.id = id;
	}

	public SubscribeId(SubscribeId other) {
		this.id = other.id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int compareTo(SubscribeId o) {
		return id.compareTo(o.id);
	}
}
