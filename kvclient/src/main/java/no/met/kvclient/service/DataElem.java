package no.met.kvclient.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class DataElem extends DataIdElement {
	public int paramID;
	public int sensor;
	public int level;
	public Instant tbtime;
	public double original;
	public double corrected;
	public String controlinfo;
	public String useinfo;
	public String cfailed;

	/* TABLE data
	stationid   INTEGER NOT NULL,
	obstime     TIMESTAMP NOT NULL,
	original    FLOAT NOT NULL,
	paramid	    INTEGER NOT NULL,
	tbtime	    TIMESTAMP NOT NULL,
	typeid	    INTEGER NOT NULL,
	sensor	    CHAR(1) DEFAULT '0',
	level	    INTEGER DEFAULT 0,
	corrected   FLOAT NOT NULL,
	controlinfo CHAR(16) DEFAULT '0000000000000000',
	useinfo     CHAR(16) DEFAULT '0000000000000000',
	cfailed TEXT DEFAULT NULL,
	 */


	public DataElem(ResultSet rd) throws SQLException{
		this(rd.getInt(1),rd.getTimestamp(2).toInstant(), rd.getDouble(3), rd.getInt(4),
				rd.getTimestamp(5).toInstant(),rd.getInt(6),Integer.parseInt(rd.getString(7)),
				rd.getInt(8), rd.getDouble(9),rd.getString(10),rd.getString(11), rd.getString(12));
	}

	public DataElem(long stationID, Instant obstime, double original, int paramID, Instant tbtime, long typeID_,
			int sensor, int level, double corrected, String controlinfo, String useinfo, String cfailed) {
		super(stationID, typeID_, obstime);

		this.paramID=paramID;
		this.sensor = sensor;
		this.level = level;
		this.tbtime = tbtime;
		this.original = original;
		this.corrected = corrected;
		this.controlinfo = controlinfo==null?"":controlinfo;
		this.useinfo = useinfo==null?"":useinfo;
		this.cfailed = cfailed==null?"":cfailed;
	}

	public DataElem(DataElem other) {
		super(other.stationID, other.typeID, other.obstime);
		this.paramID=other.paramID;
		this.sensor = other.sensor;
		this.level = other.level;
		this.tbtime = other.tbtime;
		this.original = other.original;
		this.corrected = other.corrected;
		this.controlinfo = other.controlinfo;
		this.useinfo = other.useinfo;
		this.cfailed = other.cfailed;
	}

	@Override public String toString() {
		return "sid: " + stationID+" tid: "+typeID + " ot: " + obstime +" pid: " + paramID+" s: "+sensor+" l: " + level +" o: "+ original + " c: "+corrected +" ci: "+ controlinfo +" ui: "+ useinfo+ " cf: " +cfailed + " tbt: "+tbtime;
	}
}
