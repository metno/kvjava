package kvalobs.service;

public class ModelDataElem {
	public long stationID;
	public String obstime;
	public short paramID;
	public short level;
	public long modelID;
	public double original;

	public ModelDataElem(long stationID, String obstime, short paramID, short level, long modelID, double original) {
		this.stationID = stationID;
		this.obstime = obstime;
		this.paramID = paramID;
		this.level = level;
		this.modelID = modelID;
		this.original = original;
	}

	public ModelDataElem(ModelDataElem other) {
		this.stationID = other.stationID;
		this.obstime = other.obstime;
		this.paramID = other.paramID;
		this.level = other.level;
		this.modelID = other.modelID;
		this.original = other.original;
	}
}
