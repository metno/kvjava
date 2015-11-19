package no.met.kvclient.service;

public class ObsDataList extends java.util.LinkedList<ObsData> {
	private static final long serialVersionUID = -3426183075859993004L;

	public ObsDataList(ObsDataList other) {
		super(other);
	}

	public ObsDataList() {
	}
	
	@Override
	public String toString(){
		StringBuilder builder=new StringBuilder();
		builder.append("ObsDataList #" + size()+"\n");
		
		for(ObsData data:this){
			builder.append("  data sid: " + data.stationid+" tid: "+data.typeID + " obst: " + data.obstime +" #dataElement: "+data.dataList.size() + " #textDate: " +data.textDataList.size()+"\n");
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
