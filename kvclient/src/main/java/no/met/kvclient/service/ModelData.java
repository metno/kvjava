package no.met.kvclient.service;

public class ModelData {
	long stationid;
	ModelDataElemList dataList;

	public ModelData(long stationid, ModelDataElemList dataList) {
		this.stationid = stationid;
		this.dataList = dataList;
	}

	public ModelData(ModelData other) {
		this.stationid = other.stationid;
		this.dataList = other.dataList;
	}
}
