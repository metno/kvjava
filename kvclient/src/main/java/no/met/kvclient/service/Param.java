package no.met.kvclient.service;

public class Param {
	public long paramID;
	public String name;
	public String description;
	public String unit;
	public long level_scale;
	public String comment;

	public Param(long paramID, String name, String description, String unit, long level_scale, String comment) {
		this.paramID = paramID;
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.level_scale = level_scale;
		this.comment = comment;
	}

	public Param(Param other) {
		this.paramID = other.paramID;
		this.name = other.name;
		this.description = other.description;
		this.unit = other.unit;
		this.level_scale = other.level_scale;
		this.comment = other.comment;
	}
}