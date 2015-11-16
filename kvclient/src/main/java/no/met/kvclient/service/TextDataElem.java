package no.met.kvclient.service;

public class TextDataElem {
	public long stationID;
	public String obstime;
	public String original;
	public short paramID;
	public String tbtime;
	public short typeID_;

	public TextDataElem(long stationID, String obstime, String original, short paramID, String tbtime, short typeID) {
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
