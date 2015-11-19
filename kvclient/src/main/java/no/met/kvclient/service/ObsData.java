package no.met.kvclient.service;

import java.time.Instant;

public class ObsData {
	public long stationid;
	public long typeID;
	public Instant obstime;
	public DataElemList dataList;
	public TextDataElemList textDataList;

	public ObsData(long stationid, long typeID, Instant obstime) {
		this.stationid = stationid;
		this.typeID = typeID;
		this.obstime = obstime;
		this.dataList = new DataElemList();
		this.textDataList = new TextDataElemList();
	}

	public ObsData(long stationid, long typeID, Instant obstime, TextDataElemList textDataList) {
		this.stationid = stationid;
		this.typeID = typeID;
		this.obstime = obstime;
		this.dataList = new DataElemList();
		this.textDataList = textDataList;
	}

	public ObsData(long stationid, long typeID, Instant obstime, DataElemList dataList, TextDataElemList textDataList) {
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
	
	public void addData(int paramID, int sensor, int level, Instant tbtime, double original, double corrected, String controlinfo, String useinfo, String cfailed){
		DataElem e = new DataElem(stationid, obstime, original,
				paramID, tbtime,typeID,sensor,level,corrected,controlinfo,useinfo,cfailed);
		dataList.add(e);
	}
	
	public void addTextData(int paramID, Instant tbtime, String original) {
		TextDataElem e=new TextDataElem(stationid, obstime, original,paramID, tbtime,typeID);
		textDataList.add(e);
	}

	public boolean isEmpty(){
		return dataList.isEmpty() && textDataList.isEmpty();
	}
}
