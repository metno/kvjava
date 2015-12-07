package no.met.kvclient.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import no.met.kvutil.DateTimeUtil;

public class WhichData {
	public long stationid; // 0 all stations
	public long typeid_; // 0 all typeids
	public long paramid; // 0 all paramids
	public StatusId status;
	public Instant fromTime;
	public Instant toTime;

	public WhichData(long stationid, long typeid_, long paramid, StatusId status, String fromObsTime, String toObsTime) {
		this.stationid = stationid;
		this.typeid_ = typeid_;
		this.paramid = paramid;
		this.status = status;
		this.fromTime = DateTimeUtil.parse(fromObsTime).toInstant();
		this.toTime = DateTimeUtil.parse(toObsTime).toInstant();
	}

	public WhichData(long stationid, long typeid_, long paramid, StatusId status, OffsetDateTime fromTime,
			OffsetDateTime toTime) {
		this.stationid = stationid;
		this.typeid_ = typeid_;
		this.paramid = paramid;
		this.status = status;
		this.fromTime = fromTime.toInstant();
		this.toTime = toTime.toInstant();
	}
	
	public WhichData(long stationid, long typeid_, long paramid, StatusId status, Instant fromTime,
			Instant toTime) {
		this.stationid = stationid;
		this.typeid_ = typeid_;
		this.paramid = paramid;
		this.status = status;
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	public WhichData(WhichData other) {
		this.stationid = other.stationid;
		this.typeid_ = other.typeid_;
		this.paramid = other.paramid;
		this.status = other.status;
	}

	public WhichData(long stationid, StatusId status, String fromObsTime, String toObsTime) {
		this(stationid, 0, 0, status, fromObsTime, toObsTime);
	}

	public WhichData(long stationid, StatusId status, OffsetDateTime fromObsTime, OffsetDateTime toObsTime) {
		this(stationid, 0, 0, status, fromObsTime, toObsTime);
	}

	public WhichData(long stationid, StatusId status, Instant fromObsTime, Instant toObsTime) {
		this(stationid, 0, 0, status, fromObsTime, toObsTime);
	}

	public WhichData(String fromObstTime, String toObsTime) {
		this(0, 0, 0, StatusId.All, fromObstTime, toObsTime);
	}
	
	public WhichData(Instant  fromObstTime, Instant toObsTime) {
		this(0, 0, 0, StatusId.All, fromObstTime, toObsTime);
	}

	public WhichData(OffsetDateTime fromObstTime, OffsetDateTime toObsTime) {
		this(0, 0, 0, StatusId.All, fromObstTime, toObsTime);
	}
}
