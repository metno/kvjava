package no.met.kvclient.service;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

public class ObsDataList extends TreeMap<DataIdElement, ObsData> implements Iterable<ObsData> { //java.util.LinkedList<ObsData> {
	private static final long serialVersionUID = -3426183075859993004L;

	public ObsDataList() {
        super(DataIdElement.cmp);
	}

	public void addData(DataElem data){
        ObsData d = get(data);
        if( d == null ) {
            d = new ObsData(data);
            put(data, d);
        }else {
            d.addData(data);
        }
	}

	public void addTextData(TextDataElem data){
        ObsData d = get(data);
        if( d == null ) {
            d = new ObsData(data);
            put(data, d);
        }else {
            d.addTextData(data);
        }
	}

	public void add(ObsData data) {
		put(data, data);
	}

	@Override
	public Iterator<ObsData> iterator() {
		return values().iterator();
	}

	@Override
	public String toString(){
		StringBuilder builder=new StringBuilder();
		builder.append("ObsDataList #" + size()+"\n");
		
		for(ObsData data : values()){
			builder.append("  data sid: " + data.stationID+" tid: "+data.typeID + " obst: " + data.obstime +" #dataElement: "+data.dataList.size() + " #textDate: " +data.textDataList.size()+"\n");
			for(DataElem e : data.dataList){
				builder.append("    d: pid: " + e.paramID+" s: "+e.sensor+" l: " + e.level +" o: "+ e.original + " c: "+e.corrected +" ci: "+ e.controlinfo +" ui: "+ e.useinfo+ " cf: " +e.cfailed + " tbt: "+e.tbtime+"\n");
			}
			for(TextDataElem e : data.textDataList){
				builder.append("   td: pid: " + e.paramID +" o: "+ e.original +"\n");
			}
		}
		return builder.toString();
	}
}
