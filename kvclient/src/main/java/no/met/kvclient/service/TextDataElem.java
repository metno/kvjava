package no.met.kvclient.service;

import no.met.kvutil.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TextDataElem extends DataIdElement {
	public String original;
	public int paramID;
	public Instant tbtime;

	/*
	TABLE text_data (
	stationid   INTEGER NOT NULL,
	obstime     TIMESTAMP NOT NULL,
	original    TEXT NOT NULL,
	paramid	    INTEGER NOT NULL,
	tbtime	    TIMESTAMP NOT NULL,
	typeid INTEGER NOT NULL,
	*/

	public TextDataElem(ResultSet rd) throws SQLException {
		this(rd.getInt(1),rd.getTimestamp(2).toInstant(), rd.getString(3), rd.getInt(4),
				rd.getTimestamp(5).toInstant(),rd.getInt(6));
	}
	public TextDataElem(long stationID, Instant obstime, String original, int paramID, Instant tbtime, long typeID) {
		super(stationID, typeID, obstime);
		this.original = original;
		this.paramID = paramID;
		this.tbtime = tbtime;
	}
	
	public TextDataElem(TextDataElem other) {
		super(other.stationID, other.typeID, other.obstime);
		this.original = other.original;
		this.paramID = other.paramID;
		this.tbtime = other.tbtime;
	}
	@Override public String toString() {
		return "sid: " + stationID+" tid: "+typeID + " ot: " + obstime +" pid: " + paramID+" o: "+ original +  " tbt: "+tbtime;
	}

	public String getTbTime() {
		OffsetDateTime tb=tbtime.atOffset(ZoneOffset.UTC);
		return DateTimeUtil.toString(tb, DateTimeUtil.ISO_WITH_MICRO_SECOND_xx);
	}

	public String getQuotedTbTime() {
		return "'" + getTbTime()+"'";
	}

}
