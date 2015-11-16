package no.met.kvclient.service;

import java.time.Instant;

public class ObsData {
	public long stationid;
	public int typeID;
	public Instant obstime;
	public DataElemList dataList;
	public TextDataElemList textDataList;

	public ObsData(long stationid, int typeID, Instant obstime) {
		this.stationid = stationid;
		this.typeID = typeID;
		this.obstime = obstime;
		this.dataList = new DataElemList();
		this.textDataList = new TextDataElemList();
	}

	public ObsData(long stationid, int typeID, Instant obstime, TextDataElemList textDataList) {
		this.stationid = stationid;
		this.typeID = typeID;
		this.obstime = obstime;
		this.dataList = new DataElemList();
		this.textDataList = textDataList;
	}

	public ObsData(long stationid, int typeID, Instant obstime, DataElemList dataList, TextDataElemList textDataList) {
		this.stationid = stationid;
		this.dataList = dataList;
		this.typeID = typeID;
		this.obstime = obstime;
		this.textDataList = textDataList;
	}

	public ObsData(ObsData other) {
		this.stationid = other.stationid;
		this.typeID = other.typeID;
		this.obstime = other.obstime;
		this.dataList = other.dataList;
		this.textDataList = other.textDataList;
	}

}
