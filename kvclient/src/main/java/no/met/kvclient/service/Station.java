package no.met.kvclient.service;

public class Station {
	public long stationID;
	public double lat;
	public double lon;
	public double height;
	public double maxspeed;
	public String name;
	public long wmonr;
	public long nationalnr;
	public String ICAOid;
	public String call_sign;
	public String stationstr;
	public long environmentid;
	public boolean static_;
	public String fromtime;

	public Station(long stationID, double lat, double lon, double height, double maxspeed, String name, long wmonr,
			long nationalnr, String ICAOid, String call_sign, String stationstr, long environmentid, boolean static_,
			String fromtime) {
		this.stationID = stationID;
		this.lat = lat;
		this.lon = lon;
		this.height = height;
		this.maxspeed = maxspeed;
		this.name = name;
		this.wmonr = wmonr;
		this.nationalnr = nationalnr;
		this.ICAOid = ICAOid;
		this.call_sign = call_sign;
		this.stationstr = stationstr;
		this.environmentid = environmentid;
		this.static_ = static_;
		this.fromtime = fromtime;
	}

	public Station(Station other) {
		this.stationID = other.stationID;
		this.lat = other.lat;
		this.lon = other.lon;
		this.height = other.height;
		this.maxspeed = other.maxspeed;
		this.name = other.name;
		this.wmonr = other.wmonr;
		this.nationalnr = other.nationalnr;
		this.ICAOid = other.ICAOid;
		this.call_sign = other.call_sign;
		this.stationstr = other.stationstr;
		this.environmentid = other.environmentid;
		this.static_ = other.static_;
		this.fromtime = other.fromtime;
	}
}
