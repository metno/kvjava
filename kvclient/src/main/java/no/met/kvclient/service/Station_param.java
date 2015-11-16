package no.met.kvclient.service;

public class Station_param {
	long stationid;
	long paramid;
	long level;
	int sensor;
	long fromday;
	long today;
	long hour;
	String qcx;
	String metadata;
	String desc_metadata;
	String fromtime;

	public Station_param(long stationid, long paramid, long level, int sensor, long fromday, long today, long hour,
			String qcx, String metadata, String desc_metadata, String fromtime) {
		this.stationid = stationid;
		this.paramid = paramid;
		this.level = level;
		this.sensor = sensor;
		this.fromday = fromday;
		this.today = today;
		this.hour = hour;
		this.qcx = qcx;
		this.metadata = metadata;
		this.desc_metadata = desc_metadata;
		this.fromtime = fromtime;
	}

	public Station_param(Station_param other) {
		this.stationid = other.stationid;
		this.paramid = other.paramid;
		this.level = other.level;
		this.sensor = other.sensor;
		this.fromday = other.fromday;
		this.today = other.today;
		this.hour = other.hour;
		this.qcx = other.qcx;
		this.metadata = other.metadata;
		this.desc_metadata = other.desc_metadata;
		this.fromtime = other.fromtime;
	}
}
