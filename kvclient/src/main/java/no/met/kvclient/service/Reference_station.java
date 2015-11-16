package kvalobs.service;

public class Reference_station {
	public long stationID;
	public short paramsetID;
	public String reference;

	public Reference_station(long stationID, short paramsetID, String reference) {
		this.stationID = stationID;
		this.paramsetID = paramsetID;
		this.reference = reference;
	}

	public Reference_station(Reference_station other) {
		this.stationID = other.stationID;
		this.paramsetID = other.paramsetID;
		this.reference = other.reference;
	}
}
