package no.met.kvclient.service;

public class Types {
	public long typeID_;
	public String format;
	public long earlyobs;
	public long lateobs;
	public String read;
	public String obspgm;
	public String comment;

	public Types(long typeID_, String format, long earlyobs, long lateobs, String read, String obspgm, String comment) {
		this.typeID_ = typeID_;
		this.format = format;
		this.earlyobs = earlyobs;
		this.lateobs = lateobs;
		this.read = read;
		this.obspgm = obspgm;
		this.comment = comment;
	}

	public Types(Types other) {
		this.typeID_ = other.typeID_;
		this.format = other.format;
		this.earlyobs = other.earlyobs;
		this.lateobs = other.lateobs;
		this.read = other.read;
		this.obspgm = other.obspgm;
		this.comment = other.comment;
	}
}
