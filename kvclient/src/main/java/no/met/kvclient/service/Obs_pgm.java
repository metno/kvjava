package no.met.kvclient.service;

public class Obs_pgm {
	public long stationID;
	public long paramID;
	public long level;
	public long nr_sensor;
	public long typeID_;
	public boolean collector;
	public boolean kl00;
	public boolean kl01;
	public boolean kl02;
	public boolean kl03;
	public boolean kl04;
	public boolean kl05;
	public boolean kl06;
	public boolean kl07;
	public boolean kl08;
	public boolean kl09;
	public boolean kl10;
	public boolean kl11;
	public boolean kl12;
	public boolean kl13;
	public boolean kl14;
	public boolean kl15;
	public boolean kl16;
	public boolean kl17;
	public boolean kl18;
	public boolean kl19;
	public boolean kl20;
	public boolean kl21;
	public boolean kl22;
	public boolean kl23;
	public boolean mon;
	public boolean tue;
	public boolean wed;
	public boolean thu;
	public boolean fri;
	public boolean sat;
	public boolean sun;
	public String fromtime;
	public String totime;

	public Obs_pgm(long stationID, long paramID, long level, long nr_sensor, long typeID_, boolean collector,
			boolean kl00, boolean kl01, boolean kl02, boolean kl03, boolean kl04, boolean kl05, boolean kl06,
			boolean kl07, boolean kl08, boolean kl09, boolean kl10, boolean kl11, boolean kl12, boolean kl13,
			boolean kl14, boolean kl15, boolean kl16, boolean kl17, boolean kl18, boolean kl19, boolean kl20,
			boolean kl21, boolean kl22, boolean kl23, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri,
			boolean sat, boolean sun, String fromtime, String totime) {
		this.stationID = stationID;
		this.paramID = paramID;
		this.level = level;
		this.nr_sensor = nr_sensor;
		this.typeID_ = typeID_;
		this.collector = collector;
		this.kl00 = kl00;
		this.kl01 = kl01;
		this.kl02 = kl02;
		this.kl03 = kl03;
		this.kl04 = kl04;
		this.kl05 = kl05;
		this.kl06 = kl06;
		this.kl07 = kl07;
		this.kl08 = kl08;
		this.kl09 = kl09;
		this.kl10 = kl10;
		this.kl11 = kl11;
		this.kl12 = kl12;
		this.kl13 = kl13;
		this.kl14 = kl14;
		this.kl15 = kl15;
		this.kl16 = kl16;
		this.kl17 = kl17;
		this.kl18 = kl18;
		this.kl19 = kl19;
		this.kl20 = kl20;
		this.kl21 = kl21;
		this.kl22 = kl22;
		this.kl23 = kl23;
		this.mon = mon;
		this.tue = tue;
		this.wed = wed;
		this.thu = thu;
		this.fri = fri;
		this.sat = sat;
		this.sun = sun;
		this.fromtime = fromtime;
		this.totime = totime;
	}

	public Obs_pgm(Obs_pgm other) {
		this.stationID = other.stationID;
		this.paramID = other.paramID;
		this.level = other.level;
		this.nr_sensor = other.nr_sensor;
		this.typeID_ = other.typeID_;
		this.collector = other.collector;
		this.kl00 = other.kl00;
		this.kl01 = other.kl01;
		this.kl02 = other.kl02;
		this.kl03 = other.kl03;
		this.kl04 = other.kl04;
		this.kl05 = other.kl05;
		this.kl06 = other.kl06;
		this.kl07 = other.kl07;
		this.kl08 = other.kl08;
		this.kl09 = other.kl09;
		this.kl10 = other.kl10;
		this.kl11 = other.kl11;
		this.kl12 = other.kl12;
		this.kl13 = other.kl13;
		this.kl14 = other.kl14;
		this.kl15 = other.kl15;
		this.kl16 = other.kl16;
		this.kl17 = other.kl17;
		this.kl18 = other.kl18;
		this.kl19 = other.kl19;
		this.kl20 = other.kl20;
		this.kl21 = other.kl21;
		this.kl22 = other.kl22;
		this.kl23 = other.kl23;
		this.mon = other.mon;
		this.tue = other.tue;
		this.wed = other.wed;
		this.thu = other.thu;
		this.fri = other.fri;
		this.sat = other.sat;
		this.sun = other.sun;
		this.fromtime = other.fromtime;
		this.totime = other.totime;
	}

}
