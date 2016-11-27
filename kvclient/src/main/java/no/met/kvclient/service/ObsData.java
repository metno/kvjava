package no.met.kvclient.service;

import java.time.Instant;
import static no.met.kvclient.service.DataIdElement.cmp;

public class ObsData extends DataIdElement{
	public DataElemList dataList;
	public TextDataElemList textDataList;

    public ObsData(DataElem de) {
        super(de.stationID, de.typeID, de.obstime);
        dataList = new DataElemList();
        textDataList = new TextDataElemList();
        dataList.add(de);
    }

    public ObsData(TextDataElem de) {
        super(de.stationID, de.typeID, de.obstime);
        dataList = new DataElemList();
        textDataList = new TextDataElemList();
        textDataList.add(de);
    }


    public ObsData(long stationid, long typeID, Instant obstime) {
		super(stationid, typeID, obstime);
		this.dataList = new DataElemList();
		this.textDataList = new TextDataElemList();
	}

	public ObsData(long stationid, long typeID, Instant obstime, TextDataElemList textDataList) {
		super(stationid, typeID, obstime);
		this.dataList = new DataElemList();
		this.textDataList = textDataList;
	}

	public ObsData(long stationid, long typeID, Instant obstime, DataElemList dataList, TextDataElemList textDataList) {
		super(stationid, typeID, obstime);
		this.dataList = dataList;
		this.textDataList = textDataList;
	}

	public void addData(int paramID, int sensor, int level, Instant tbtime, double original, double corrected, String controlinfo, String useinfo, String cfailed){
		DataElem e = new DataElem(stationID, obstime, original,
				paramID, tbtime,typeID,sensor,level,corrected,controlinfo,useinfo,cfailed);
		dataList.add(e);
	}

	public boolean addData(DataElem d){
		if( cmp.compare(this,d) != 0)
		    return false;

	    dataList.add(d);
		return true;
	}

	public void addTextData(int paramID, String original, Instant tbtime) {
		TextDataElem e=new TextDataElem(stationID, obstime, original,paramID, tbtime,typeID);
		textDataList.add(e);
	}

	public boolean addTextData(TextDataElem d){
		if( cmp.compare(this, d) != 0)
		    return false;
		textDataList.add(d);
		return true;
	}

	public boolean isEmpty(){
		return dataList.isEmpty() && textDataList.isEmpty();
	}

	public int size() { return dataList.size() + textDataList.size();}
}
