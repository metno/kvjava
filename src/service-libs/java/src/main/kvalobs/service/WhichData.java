package kvalobs.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class WhichData {
	public long stationid; // 0 all stations
	public long typeid_; // 0 all typeids
	public long paramid; // 0 all paramids
	public StatusId status;
	public OffsetDateTime fromTime;
	public OffsetDateTime toTime;
	public String fromObsTime;
	public String toObsTime;

	WhichData(long stationid, long typeid_, long paramid, StatusId status, String fromObsTime, String toObsTime) {
		this.stationid = stationid;
		this.typeid_ = typeid_;
		this.paramid = paramid;
		this.status = status;
		this.fromObsTime = fromObsTime;
		this.toObsTime = toObsTime;
		this.fromTime = OffsetDateTime.parse(fromObsTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		this.toTime = OffsetDateTime.parse(toObsTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	WhichData(long stationid, long typeid_, long paramid, StatusId status, OffsetDateTime fromTime,
			OffsetDateTime toTime) {
		this.stationid = stationid;
		this.typeid_ = typeid_;
		this.paramid = paramid;
		this.status = status;
		this.fromTime = fromTime;
		this.toTime = toTime;

		this.fromObsTime = fromTime.format(DateTimeFormatter.ISO_INSTANT);
		this.toObsTime = toTime.format(DateTimeFormatter.ISO_INSTANT);
	}

	WhichData(WhichData other) {
		this.stationid = other.stationid;
		this.typeid_ = other.typeid_;
		this.paramid = other.paramid;
		this.status = other.status;
		this.fromObsTime = other.fromObsTime;
		this.toObsTime = other.toObsTime;
	}

	WhichData(long stationid, StatusId status, String fromObsTime, String toObsTime) {
		this(stationid, 0, 0, status, fromObsTime, toObsTime);
	}

	WhichData(long stationid, StatusId status, OffsetDateTime fromObsTime, OffsetDateTime toObsTime) {
		this(stationid, 0, 0, status, fromObsTime, toObsTime);
	}

	WhichData(String fromObstTime, String toObsTime) {
		this(0, 0, 0, StatusId.All, fromObstTime, toObsTime);
	}

	WhichData(OffsetDateTime fromObstTime, OffsetDateTime toObsTime) {
		this(0, 0, 0, StatusId.All, fromObstTime, toObsTime);
	}
}
