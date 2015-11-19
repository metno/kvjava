package no.met.kvclient.service;

import java.time.Instant;

public class DataElem {
	public long stationID;
	public long typeID;
	public int paramID;
	public int sensor;
	public int level;
	public Instant obstime;
	public Instant tbtime;
	public double original;
	public double corrected;
	public String controlinfo;
	public String useinfo;
	public String cfailed;

	public DataElem(long stationID, Instant obstime, double original, int paramID, Instant tbtime, long typeID_,
			int sensor, int level, double corrected, String controlinfo, String useinfo, String cfailed) {
		this.stationID = stationID;
		this.typeID = typeID_;
		this.paramID = paramID;
		this.sensor = sensor;
		this.level = level;
		this.obstime = obstime;
		this.tbtime = tbtime;
		this.original = original;
		this.corrected = corrected;
		this.controlinfo = controlinfo==null?"":controlinfo;
		this.useinfo = useinfo==null?"":useinfo;
		this.cfailed = cfailed==null?"":cfailed;
	}

	public DataElem(DataElem other) {
		this.stationID = other.stationID;
		this.typeID = other.typeID;
		this.paramID = other.paramID;
		this.sensor = other.sensor;
		this.level = other.level;
		this.obstime = other.obstime;
		this.tbtime = other.tbtime;
		this.original = other.original;
		this.corrected = other.corrected;
		this.controlinfo = other.controlinfo;
		this.useinfo = other.useinfo;
		this.cfailed = other.cfailed;
	}
}
