package no.met.kvclient.service;

import java.time.Instant;

public class TextDataElem {
	public long stationID;
	public Instant obstime;
	public String original;
	public int paramID;
	public Instant tbtime;
	public long typeID_;

	public TextDataElem(long stationID, Instant obstime, String original, int paramID, Instant tbtime, long typeID) {
		this.stationID = stationID;
		this.obstime = obstime;
		this.original = original;
		this.paramID = paramID;
		this.tbtime = tbtime;
		this.typeID_ = typeID;
	}
	
	public TextDataElem(TextDataElem other) {
		this.stationID = other.stationID;
		this.obstime = other.obstime;
		this.original = other.original;
		this.paramID = other.paramID;
		this.tbtime = other.tbtime;
		this.typeID_ = other.typeID_;
	}

}
